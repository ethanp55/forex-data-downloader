package oanda

import sttp.client4.quick.*
import sttp.client4.{Request, Response}
import play.api.libs.json.*

object DataDownloader {
  private val baseUrl: String =
    "https://api-fxpractice.oanda.com/v3/instruments"
  private val currencyPair: String = "EUR_USD"
  private val granularity: String = "H1"
  private val price: String = "M"
  val apiToken: String = Config.apiToken
  private val dateFormat: String = "UNIX"

  private val headers = Map(
    "price" -> price,
    "granularity" -> granularity,
    "count" -> "1000"
  )
  val uri = uri"$baseUrl/$currencyPair/candles?$headers"

  val request: Request[String] = quickRequest
    .get(uri)
    .auth
    .bearer(apiToken)
    .header("Accept-Datetime-Format", dateFormat)
    .header("Content-Type", "application/json")
  val response: Response[String] = request.send()
}

@main
def main(): Unit = {
  println(DataDownloader.response)
}
