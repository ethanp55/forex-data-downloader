package oanda

import sttp.client4.quick.*
import sttp.client4.{Request, Response}
import play.api.libs.json.*
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}

object DataDownloader {
  private val baseUrl: String =
    "https://api-fxpractice.oanda.com/v3/instruments"

  private val dateFormat: String = "UNIX"

  private val apiToken: String = Config.apiToken

  def dateTimeToUnix(localDateTime: LocalDateTime): String =
    localDateTime.toEpochSecond(ZoneOffset.UTC).toString

  def downloadCandles(): Seq[Candle] = {
    val currencyPair: String = "EUR_USD"
    val granularity: String = "H1"
    val price: String = "M"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val fromDate = LocalDateTime.parse("2025-01-01 00:00:00", formatter)
    val toDate = LocalDateTime.parse("2025-05-01 00:00:00", formatter)

    val headers = Map(
      "price" -> price,
      "granularity" -> granularity,
      "from" -> DataDownloader.dateTimeToUnix(fromDate),
      "to" -> DataDownloader.dateTimeToUnix(toDate)
    )
    val uri = uri"$baseUrl/$currencyPair/candles?$headers"

    val request: Request[String] = quickRequest
      .get(uri)
      .auth
      .bearer(apiToken)
      .header("Accept-Datetime-Format", dateFormat)
      .header("Content-Type", "application/json")
    val response: Response[String] = request.send()
    val candlesJson: JsValue = Json.parse(response.body)("candles")
    candlesJson.as[Seq[Candle]]
  }
}

@main
def main(): Unit = {
//  println(DataDownloader.downloadCandles())
}

/*
 * TODO:
  - Date range (to, from, iteratively build)
  - Enums for pair, granularity, and prices (b/m/a)
  - Request to controller
  - Update data downloader to use candle requests (prev step)
  - Error handling
  - Controller
  - Unit tests
  - Controller tests
 * */
