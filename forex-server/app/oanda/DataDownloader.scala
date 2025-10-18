package oanda

import sttp.client4.quick.*
import sttp.client4.Request
import play.api.libs.json.*
import request.{CandlesDownloadRequest, PricingComponent}
import sttp.model.StatusCode
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.{LocalDateTime, ZoneOffset}
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.boundary
import scala.util.boundary.{Label, break}

object DataDownloader {
  private val baseUrl: String =
    "https://api-fxpractice.oanda.com/v3/instruments"
  private val dateFormat: String = "UNIX"
  private val apiToken: String = Config.apiToken
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
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

  private def dateTimeToUnix(localDateTime: LocalDateTime): String =
    localDateTime.toEpochSecond(ZoneOffset.UTC).toString

  private def createRequests(
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
          "from" -> DataDownloader.dateTimeToUnix(currFromTime),
          "to" -> DataDownloader.dateTimeToUnix(currToTime)
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
            s"Could not convert $requestedFromDate and/or $requestedToDate to ${formatter.toString}"
          )
        )
    }
  }

  private def sendRequests(
      requests: Seq[Request[String]]
  )(implicit executionContext: ExecutionContext) = {
    @tailrec
    def _sendWithRetry(
        request: Request[String],
        numRetries: Int
    ): Either[ServerError, Seq[Candle]] = {
      val response = request.send()

      response.code match {
        case StatusCode.Ok =>
          val candlesJson: JsValue = Json.parse(response.body)("candles")
          val candles = candlesJson.as[Seq[Candle]]

          Right(candles)

        case x if retryStatusCodes.contains(x) && numRetries > 0 =>
          Thread.sleep(nSecondsBetweenRetries * 1000)
          _sendWithRetry(request, numRetries - 1)

        case _ =>
          Left(OandaApiServerError(response.body, response.code.toString))
      }
    }

    val requestFutures =
      Future.sequence(
        requests.map(request => Future(_sendWithRetry(request, nRetries)))
      )

    implicit val boundaryLabel: Label[ServerError] = new Label[ServerError]
    requestFutures.map { futureSequence =>
      val candleSequences: Either[ServerError, Seq[Candle]] = boundary {
        val candles = futureSequence.collect {
          case Left(error)    => break(error)
          case Right(candles) => candles
        }

        Right(candles.flatten)
      }

      candleSequences
    }
  }

  def downloadCandles(candlesDownloadRequest: CandlesDownloadRequest)(implicit
      executionContext: ExecutionContext
  ): Future[Either[ServerError, Seq[Candle]]] = {
    val requestsEither = createRequests(candlesDownloadRequest)
    requestsEither match {
      case Left(error)     => Future(Left(error))
      case Right(requests) => sendRequests(requests)
    }
  }
}

/*
 * TODO:
  - Unit tests (might need to refactor code)
  - Controller tests
  - Add documentation
 * */
