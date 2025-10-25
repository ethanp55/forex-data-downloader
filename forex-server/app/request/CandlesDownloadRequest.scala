package request

import play.api.libs.json.{JsObject, JsResult, JsValue, Json, OFormat}

/** Case class used to represent user requests for downloading historical candle
  * data.
  *
  * @param currencyPair
  *   The currency pair to download data for.
  * @param granularity
  *   The granularity to retrieve (minute candles, 30-minute candles, hourly
  *   candles, etc.)
  * @param pricingComponents
  *   Which prices to include in the candles (bid, mid, and/or ask).
  * @param fromDate
  *   The start datetime to download data for. Should be in yyyy-MM-dd HH:mm:ss"
  *   format.
  * @param toDate
  *   The end datetime to download data for. Should be in yyyy-MM-dd HH:mm:ss"
  *   format.
  */
case class CandlesDownloadRequest(
    currencyPair: CurrencyPair,
    granularity: Granularity,
    pricingComponents: Seq[PricingComponent],
    fromDate: String,
    toDate: String
)

/** Companion CandlesDownloadRequest object used for defining JSON
  * functionality.
  */
object CandlesDownloadRequest {
  implicit val candlesDownloadRequestFormat: OFormat[CandlesDownloadRequest] =
    Json.format[CandlesDownloadRequest]
}
