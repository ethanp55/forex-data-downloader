package oanda

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.pattern.after
import sttp.client4.SyncBackend
import sttp.client4.quick.{UriContext, quickRequest}
import sttp.client4.Request
import play.api.libs.json.*
import request.{CandlesDownloadRequest, PricingComponent}
import sttp.model.StatusCode
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.{LocalDateTime, ZoneOffset}
import javax.inject.*
import scala.annotation.tailrec
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CandleDownloader @Inject() (backend: SyncBackend) {
  private val baseUrl: String =
    "https://api-fxpractice.oanda.com/v3/instruments"
  private val dateFormat: String = "UNIX"
  private val apiToken: String = Config.apiToken
  private val timeFormat: String = "yyyy-MM-dd HH:mm:ss"
  private val formatter = DateTimeFormatter.ofPattern(timeFormat)
  private val nRetries: Int = 3
  private val nSecondsBetweenRetries: Int = 1
  private val retryStatusCodes: Seq[StatusCode] = Seq(
    StatusCode.InternalServerError,
    StatusCode.BadGateway,
    StatusCode.ServiceUnavailable,
    StatusCode.GatewayTimeout
  )
  // Oanda's API only sends a max of 5000 candles per request
  private val maxCandlesPerRequest: Int = 5000
  private val waitTime: Int = 20

  final protected def dateTimeToUnix(localDateTime: LocalDateTime): String =
    localDateTime.toEpochSecond(ZoneOffset.UTC).toString

  final protected def createRequests(
      candlesDownloadRequest: CandlesDownloadRequest
  ): Either[ServerError, Seq[Request[String]]] = {
    try {
      val fromDate =
        LocalDateTime.parse(candlesDownloadRequest.fromDate, formatter)
      val toDate = LocalDateTime.parse(candlesDownloadRequest.toDate, formatter)
      val numMinutesPerRange: Long =
        maxCandlesPerRequest * candlesDownloadRequest.granularity.toMinutes

      @tailrec
      def _createDateRanges(
          currentFromDate: LocalDateTime,
          dateRanges: Seq[(LocalDateTime, LocalDateTime)]
      ): Seq[(LocalDateTime, LocalDateTime)] = {
        if (currentFromDate.isBefore(toDate)) {
          val newToDate = currentFromDate.plusMinutes(numMinutesPerRange)
          val currentToDate =
            if (newToDate.isBefore(toDate)) newToDate else toDate

          _createDateRanges(
            currentToDate,
            dateRanges :+ (currentFromDate, currentToDate)
          )
        } else {
          dateRanges
        }
      }

      val dateRanges = _createDateRanges(fromDate, Seq.empty)
      val requests = dateRanges.map { (currFromTime, currToTime) =>
        val headers = Map(
          "price" -> PricingComponent.combine(
            candlesDownloadRequest.pricingComponents
          ),
          "granularity" -> candlesDownloadRequest.granularity.toString,
          "from" -> dateTimeToUnix(currFromTime),
          "to" -> dateTimeToUnix(currToTime)
        )

        val uri =
          uri"$baseUrl/${candlesDownloadRequest.currencyPair.toString}/candles?$headers"

        quickRequest
          .get(uri)
          .auth
          .bearer(apiToken)
          .header("Accept-Datetime-Format", dateFormat)
          .header("Content-Type", "application/json")
      }

      Right(requests)
    } catch {
      case _: DateTimeParseException =>
        val requestedFromDate = candlesDownloadRequest.fromDate
        val requestedToDate = candlesDownloadRequest.toDate

        Left(
          DateTimeParseServerError(
            s"Could not convert $requestedFromDate and/or $requestedToDate; expected format = $timeFormat"
          )
        )
    }
  }

  final protected def sendRequests(
      requests: Seq[Request[String]]
  )(implicit
      executionContext: ExecutionContext,
      actorSystem: ActorSystem
  ): Future[Either[ServerError, Seq[Candle]]] = {
    @tailrec
    def _sendWithRetry(
        request: Request[String],
        numRetries: Int
    ): Either[ServerError, Seq[Candle]] = {
      val response = request.send(backend)

      response.code match {
        case StatusCode.Ok =>
          val candlesJson: JsValue = Json.parse(response.body)("candles")
          val candles = candlesJson.as[Seq[Candle]]

          Right(candles)

        case x if retryStatusCodes.contains(x) && numRetries > 0 =>
          after(nSecondsBetweenRetries.seconds, using = actorSystem.scheduler)
          _sendWithRetry(request, numRetries - 1)

        case _ =>
          Left(OandaApiServerError(response.body))
      }
    }

    val requestFutures =
      Future.sequence(
        requests.map(request => Future(_sendWithRetry(request, nRetries)))
      )

    requestFutures.map { futureSequence =>
      val errorOption = futureSequence.collectFirst { case Left(error) =>
        error
      }
      errorOption match {
        case Some(error) => Left(error)
        case None        =>
          val allCandles = futureSequence.collect { case Right(candles) =>
            candles
          }.flatten
          Right(allCandles)
      }
    }
  }

  def downloadCandles(candlesDownloadRequest: CandlesDownloadRequest)(implicit
      executionContext: ExecutionContext,
      actorSystem: ActorSystem
  ): Future[Either[ServerError, Seq[Candle]]] = {
    val requestsEither = createRequests(candlesDownloadRequest)
    requestsEither match {
      case Left(error)     => Future(Left(error))
      case Right(requests) => sendRequests(requests)
    }
  }
}
