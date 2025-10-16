package oanda

sealed trait Error {
  val code: String
  val message: String
}

case class DateTimeParseError(message: String) extends Error {
  override val code: String = "INVALID_DATE_STRING"
}

case class OandaApiError(message: String, statusCode: String) extends Error {
  override val code: String = "OANDA_API_ERROR"
}
