package request

import play.api.libs.json.{JsObject, JsResult, JsValue, Json, OFormat}

case class CandlesDownloadRequest(
    currencyPair: CurrencyPair,
    granularity: Granularity,
    pricingComponents: Seq[PricingComponent],
    fromDate: String,
    toDate: String
)

object CandlesDownloadRequest {
  implicit val candlesDownloadRequestFormat: OFormat[CandlesDownloadRequest] =
    Json.format[CandlesDownloadRequest]
}
