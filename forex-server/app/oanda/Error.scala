package oanda

import play.api.libs.json.{Json, OFormat}

sealed trait Error {
  val code: String
  val message: String
}

object Error {
  implicit val errorFormat: OFormat[Error] = Json.format[Error]
}

case class DateTimeParseError(message: String) extends Error {
  override val code: String = "INVALID_DATE_STRING"
}

object DateTimeParseError {
  implicit val dateTimeParseErrorFormat: OFormat[DateTimeParseError] =
    Json.format[DateTimeParseError]
}

case class OandaApiError(message: String, statusCode: String) extends Error {
  override val code: String = "OANDA_API_ERROR"
}

object OandaApiError {
  implicit val oandaApiErrorFormat: OFormat[OandaApiError] =
    Json.format[OandaApiError]
}
