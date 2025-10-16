package request

sealed trait CurrencyPair {
  def stringFormat: String
}

// Major pairs
case object EurUsd extends CurrencyPair {
  override def stringFormat: String = "EUR_USD"
}

case object UsdJpy extends CurrencyPair {
  override def stringFormat: String = "USD_JPY"
}

case object GbpUsd extends CurrencyPair {
  override def stringFormat: String = "GBP_USD"
}

case object UsdCad extends CurrencyPair {
  override def stringFormat: String = "USD_CAD"
}

case object AudUsd extends CurrencyPair {
  override def stringFormat: String = "AUD_USD"
}

case object UsdChf extends CurrencyPair {
  override def stringFormat: String = "USD_CHF"
}

case object NzdUsd extends CurrencyPair {
  override def stringFormat: String = "NZD_USD"
}

// Popular minor pairs
case object EurGbp extends CurrencyPair {
  override def stringFormat: String = "EUR_GBP"
}

case object EurJpy extends CurrencyPair {
  override def stringFormat: String = "EUR_JPY"
}

case object GbpJpy extends CurrencyPair {
  override def stringFormat: String = "GBP_JPY"
}

case object GbpCad extends CurrencyPair {
  override def stringFormat: String = "GBP_CAD"
}

case object AudJpy extends CurrencyPair {
  override def stringFormat: String = "AUD_JPY"
}

case object EurAud extends CurrencyPair {
  override def stringFormat: String = "EUR_AUD"
}

case object EurCad extends CurrencyPair {
  override def stringFormat: String = "EUR_CAD"
}

case object EurChf extends CurrencyPair {
  override def stringFormat: String = "EUR_CHF"
}

case object CadJpy extends CurrencyPair {
  override def stringFormat: String = "CAD_JPY"
}

case object GbpChf extends CurrencyPair {
  override def stringFormat: String = "GBP_CHF"
}

case object NzdJpy extends CurrencyPair {
  override def stringFormat: String = "NZD_JPY"
}

case object ChfJpy extends CurrencyPair {
  override def stringFormat: String = "CHF_JPY"
}

case object NzdCad extends CurrencyPair {
  override def stringFormat: String = "NZD_CAD"
}
