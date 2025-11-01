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
import scala.concurrent.Future

/** Helper class for making GET requests to Oanda (for downloading historical
  * data). The @Singleton decorator is used because this class does not track
  * any internal state, so we only need one instance.
  *
  * @param backend
  *   SyncBackend from the sttp library for sending HTTP requests.
  * @param ec
  *   Custom OandaExecutionContext used for sending requests to Oanda with a
  *   specific thread pool (to prevent blocking in the CandleController).
  */
@Singleton
class CandleDownloader @Inject() (backend: SyncBackend)(implicit
    ec: OandaExecutionContext
) {
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

  /** Helper function that converts LocalDateTime objects to UNIX (Oanda expects
    * times to be in UNIX format).
    *
    * @param localDateTime
    *   Datetime object to convert to UNIX.
    * @return
    *   A string representing a datetime object in UNIX format.
    */
  final protected def dateTimeToUnix(localDateTime: LocalDateTime): String =
    localDateTime.toEpochSecond(ZoneOffset.UTC).toString

  /** Helper function that converts a CandlesDownloadRequest to a sequence of
    * sttp Request objects. A sequence is needed because Oanda can only return a
    * max of 5000 candles, so multiple requests might need to be sent to Oanda
    * to retrieve candles for the full date range.
    *
    * @param candlesDownloadRequest
    *   A custom CandlesDownloadRequest object that stores information about
    *   which currency pair, granularity, date range, etc. to download data for.
    * @return
    *   An Either object. If the from and to dates in the request cannot be
    *   converted to UNIX, a Left with an error (indicating the issue) is
    *   returned. Otherwise, a Right with a sequence of sttp requests is
    *   returned.
    */
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
          "to" -> dateTimeToUnix(currToTime),
          "dailyAlignment" -> 0,
          "alignmentTimezone" -> "UTC"
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

  /** Function that sends sttp Request objects to Oanda for downloading
    * historical candle data.
    *
    * @param requests
    *   Sequence of sttp requests to send to Oanda.
    * @param actorSystem
    *   Pekko ActorSystem used to create a small delay before retrying a failed
    *   request to Oanda (if Oanda returns a vague error, this function will
    *   wait temporarily and attempt to send the request again).
    * @return
    *   A Future containing an Either. A Future is used because we do not want
    *   the code to block while it waits for Oanda to return the requested
    *   results. If any errors occur, a Left is returned with the first detected
    *   error. If everything succeeds, a Right is returned with a sequence of
    *   Candle objects (the candles corresponding to what was requested by the
    *   user).
    */
  final protected def sendRequests(
      requests: Seq[Request[String]]
  )(implicit
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

  /** The main public facing function for requesting historical candle data.
    * This function first creates a sequence of sttp Request objects and then
    * sends them to Oanda.
    *
    * @param candlesDownloadRequest
    *   A custom CandlesDownloadRequest object that stores information about
    *   which currency pair, granularity, date range, etc. to download data for.
    * @param actorSystem
    *   Pekko ActorSystem used to create a small delay before retrying a failed
    *   request to Oanda (if Oanda returns a vague error, this function will
    *   wait temporarily and attempt to send the request again).
    * @return
    *   A Future containing an Either. A Future is used because we do not want
    *   the code to block while it waits for Oanda to return the requested
    *   results. If any errors occur, a Left is returned with the first detected
    *   error. If everything succeeds, a Right is returned with a sequence of
    *   Candle objects (the candles corresponding to what was requested by the
    *   user).
    */
  def downloadCandles(candlesDownloadRequest: CandlesDownloadRequest)(implicit
      actorSystem: ActorSystem
  ): Future[Either[ServerError, Seq[Candle]]] = {
    val requestsEither = createRequests(candlesDownloadRequest)
    requestsEither match {
      case Left(error)     => Future(Left(error))
      case Right(requests) => sendRequests(requests)
    }
  }
}
