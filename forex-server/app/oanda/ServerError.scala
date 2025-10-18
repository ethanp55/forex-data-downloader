package oanda

import play.api.libs.json.{Json, OFormat}

sealed trait ServerError {
  val code: String
  val message: String
}

object ServerError {
  implicit val errorFormat: OFormat[ServerError] = Json.format[ServerError]
}

case class DateTimeParseServerError(message: String) extends ServerError {
  override val code: String = "INVALID_DATE_STRING"
}

object DateTimeParseServerError {
  implicit val dateTimeParseErrorFormat: OFormat[DateTimeParseServerError] =
    Json.format[DateTimeParseServerError]
}

case class OandaApiServerError(message: String, statusCode: String)
    extends ServerError {
  override val code: String = "OANDA_API_ERROR"
}

object OandaApiServerError {
  implicit val oandaApiErrorFormat: OFormat[OandaApiServerError] =
    Json.format[OandaApiServerError]
}

case class UnknownServerError(message: String) extends ServerError {
  override val code: String = "UNKNOWN_ERROR"
}

object UnknownServerError {
  implicit val unknownServerErrorFormat: OFormat[UnknownServerError] =
    Json.format[UnknownServerError]
}
