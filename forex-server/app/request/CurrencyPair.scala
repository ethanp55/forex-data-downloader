package request

import play.api.libs.json._

/** Sealed trait for defining CurrencyPair enums.
  */
sealed trait CurrencyPair

/** Companion CurrencyPair object used for defining JSON functionality.
  */
object CurrencyPair {
  implicit val currencyPairFormat: Format[CurrencyPair] =
    new Format[CurrencyPair] {
      def writes(o: CurrencyPair): JsValue = o match {
        case EUR_USD => JsString("EUR_USD")
        case USD_JPY => JsString("USD_JPY")
        case GBP_USD => JsString("GBP_USD")
        case USD_CAD => JsString("USD_CAD")
        case AUD_USD => JsString("AUD_USD")
        case USD_CHF => JsString("USD_CHF")
        case NZD_USD => JsString("NZD_USD")
        case EUR_GBP => JsString("EUR_GBP")
        case EUR_JPY => JsString("EUR_JPY")
        case GBP_JPY => JsString("GBP_JPY")
        case GBP_CAD => JsString("GBP_CAD")
        case AUD_JPY => JsString("AUD_JPY")
        case EUR_AUD => JsString("EUR_AUD")
        case EUR_CAD => JsString("EUR_CAD")
        case EUR_CHF => JsString("EUR_CHF")
        case CAD_JPY => JsString("CAD_JPY")
        case GBP_CHF => JsString("GBP_CHF")
        case NZD_JPY => JsString("NZD_JPY")
        case CHF_JPY => JsString("CHF_JPY")
        case NZD_CAD => JsString("NZD_CAD")
      }

      def reads(json: JsValue): JsResult[CurrencyPair] = json match {
        case JsString("EUR_USD") => JsSuccess(EUR_USD)
        case JsString("USD_JPY") => JsSuccess(USD_JPY)
        case JsString("GBP_USD") => JsSuccess(GBP_USD)
        case JsString("USD_CAD") => JsSuccess(USD_CAD)
        case JsString("AUD_USD") => JsSuccess(AUD_USD)
        case JsString("USD_CHF") => JsSuccess(USD_CHF)
        case JsString("NZD_USD") => JsSuccess(NZD_USD)
        case JsString("EUR_GBP") => JsSuccess(EUR_GBP)
        case JsString("EUR_JPY") => JsSuccess(EUR_JPY)
        case JsString("GBP_JPY") => JsSuccess(GBP_JPY)
        case JsString("GBP_CAD") => JsSuccess(GBP_CAD)
        case JsString("AUD_JPY") => JsSuccess(AUD_JPY)
        case JsString("EUR_AUD") => JsSuccess(EUR_AUD)
        case JsString("EUR_CAD") => JsSuccess(EUR_CAD)
        case JsString("EUR_CHF") => JsSuccess(EUR_CHF)
        case JsString("CAD_JPY") => JsSuccess(CAD_JPY)
        case JsString("GBP_CHF") => JsSuccess(GBP_CHF)
        case JsString("NZD_JPY") => JsSuccess(NZD_JPY)
        case JsString("CHF_JPY") => JsSuccess(CHF_JPY)
        case JsString("NZD_CAD") => JsSuccess(NZD_CAD)
        case _                   => JsError("Unknown CurrencyPair")
      }
    }
}

/** Case objects that represent the major currency pairs.
  */
case object EUR_USD extends CurrencyPair
case object USD_JPY extends CurrencyPair
case object GBP_USD extends CurrencyPair
case object USD_CAD extends CurrencyPair
case object AUD_USD extends CurrencyPair
case object USD_CHF extends CurrencyPair
case object NZD_USD extends CurrencyPair

/** Case objects that represent some of the minor currency pairs.
  */
case object EUR_GBP extends CurrencyPair
case object EUR_JPY extends CurrencyPair
case object GBP_JPY extends CurrencyPair
case object GBP_CAD extends CurrencyPair
case object AUD_JPY extends CurrencyPair
case object EUR_AUD extends CurrencyPair
case object EUR_CAD extends CurrencyPair
case object EUR_CHF extends CurrencyPair
case object CAD_JPY extends CurrencyPair
case object GBP_CHF extends CurrencyPair
case object NZD_JPY extends CurrencyPair
case object CHF_JPY extends CurrencyPair
case object NZD_CAD extends CurrencyPair
