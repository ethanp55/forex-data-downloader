package request

sealed trait CurrencyPair

// Major pairs
case object EUR_USD extends CurrencyPair
case object USD_JPY extends CurrencyPair
case object GBP_USD extends CurrencyPair
case object USD_CAD extends CurrencyPair
case object AUD_USD extends CurrencyPair
case object USD_CHF extends CurrencyPair
case object NZD_USD extends CurrencyPair

// Popular minor pairs
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
