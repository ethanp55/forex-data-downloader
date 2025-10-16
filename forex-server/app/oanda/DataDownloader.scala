package oanda

import sttp.client4.quick.*
import sttp.client4.{Request, Response}
import play.api.libs.json.*
import request.{
  Ask,
  Bid,
  CandlesDownloadRequest,
  EUR_USD,
  H1,
  Mid,
  PricingComponent
}
import sttp.model.StatusCode

import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.{LocalDateTime, ZoneOffset}
import scala.annotation.tailrec

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

  private def dateTimeToUnix(localDateTime: LocalDateTime): String =
    localDateTime.toEpochSecond(ZoneOffset.UTC).toString

  private def createRequest(
      candlesDownloadRequest: CandlesDownloadRequest
  ): Either[Error, Request[String]] = {
    try {
      val fromDate =
        LocalDateTime.parse(candlesDownloadRequest.fromDate, formatter)
      val toDate = LocalDateTime.parse(candlesDownloadRequest.toDate, formatter)
      val headers = Map(
        "price" -> PricingComponent.combine(
          candlesDownloadRequest.pricingComponents
        ),
        "granularity" -> candlesDownloadRequest.granularity.toString,
        "from" -> DataDownloader.dateTimeToUnix(fromDate),
        "to" -> DataDownloader.dateTimeToUnix(toDate)
      )
      val uri =
        uri"$baseUrl/${candlesDownloadRequest.currencyPair.toString}/candles?$headers"

      val request: Request[String] = quickRequest
        .get(uri)
        .auth
        .bearer(apiToken)
        .header("Accept-Datetime-Format", dateFormat)
        .header("Content-Type", "application/json")
      Right(request)
    } catch {
      case _: DateTimeParseException =>
        val requestedFromDate = candlesDownloadRequest.fromDate
        val requestedToDate = candlesDownloadRequest.toDate
        Left(
          DateTimeParseError(
            s"Could not convert $requestedFromDate and/or $requestedToDate to ${formatter.toString}"
          )
        )
    }
  }

  @tailrec
  private def sendRequest(
      request: Request[String],
      numRetries: Int = nRetries
  ): Either[Error, Seq[Candle]] = {
    val response = request.send()
    response.code match {
      case StatusCode.Ok =>
        val candlesJson: JsValue = Json.parse(response.body)("candles")
        val candles = candlesJson.as[Seq[Candle]]
        Right(candles)
      case x if retryStatusCodes.contains(x) && numRetries > 0 =>
        Thread.sleep(nSecondsBetweenRetries * 1000)
        sendRequest(request, numRetries - 1)
      case _ => Left(OandaApiError(response.body, response.code.toString))
    }
  }

  def downloadCandles(
      candlesDownloadRequest: CandlesDownloadRequest
  ): Either[Error, Seq[Candle]] = {
    val requestEither = createRequest(candlesDownloadRequest)
    requestEither.flatMap(sendRequest(_))
  }
}

@main
def main(): Unit = {
  val request = CandlesDownloadRequest(
    EUR_USD,
    H1,
    Seq(Ask, Bid, Mid),
    "2025-01-01 00:00:00",
    "2025-05-01 00:00:00"
  )
  println(DataDownloader.downloadCandles(request))
}

/*
 * TODO:
  - Date range (to, from, iteratively build)
  - Controller
  - Unit tests
  - Controller tests
 * */
