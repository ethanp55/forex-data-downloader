package oanda

import play.api.libs.json.{JsError, JsValue, Json, Reads, Writes}

sealed trait ServerError {
  val code: String
  val message: String
}

object ServerError {
  implicit val serverErrorWrites: Writes[ServerError] =
    (error: ServerError) =>
      Json.obj(
        "code" -> error.code,
        "message" -> error.message
      )

  implicit val serverErrorReads: Reads[ServerError] =
    (json: JsValue) => {
      (json \ "code").asOpt[String] match {
        case Some("INVALID_DATE_STRING") =>
          json.validate[DateTimeParseServerError]
        case Some("OANDA_API_ERROR") =>
          json.validate[OandaApiServerError]
        case Some("UNKNOWN_ERROR") =>
          json.validate[UnknownServerError]
        case _ =>
          JsError("Unknown or missing error code")
      }
    }
}

case class DateTimeParseServerError(message: String) extends ServerError {
  override val code = "INVALID_DATE_STRING"
}

object DateTimeParseServerError {
  implicit val dateTimeParseServerErrorWrites
      : Writes[DateTimeParseServerError] =
    (error: DateTimeParseServerError) =>
      Json.obj(
        "code" -> error.code,
        "message" -> error.message
      )

  implicit val dateTimeParseServerErrorReads: Reads[DateTimeParseServerError] =
    (json: JsValue) => {
      (json \ "code").asOpt[String] match {
        case Some("INVALID_DATE_STRING") =>
          for {
            message <- (json \ "message").validate[String]
          } yield DateTimeParseServerError(message)
        case _ =>
          JsError("Unknown or missing error code")
      }
    }
}

case class OandaApiServerError(
    message: String,
    code: String = "OANDA_API_ERROR"
) extends ServerError

object OandaApiServerError {
  implicit val oandaApiServerErrorWrites: Writes[OandaApiServerError] =
    (error: OandaApiServerError) =>
      Json.obj(
        "code" -> error.code,
        "message" -> error.message
      )

  implicit val oandaApiServerErrorReads: Reads[OandaApiServerError] =
    (json: JsValue) => {
      (json \ "code").asOpt[String] match {
        case Some("OANDA_API_ERROR") =>
          for {
            message <- (json \ "message").validate[String]
          } yield OandaApiServerError(message)
        case _ =>
          JsError("Unknown or missing error code")
      }
    }
}

case class UnknownServerError(message: String, code: String = "UNKNOWN_ERROR")
    extends ServerError

object UnknownServerError {
  implicit val unknownServerErrorWrites: Writes[UnknownServerError] =
    (error: UnknownServerError) =>
      Json.obj(
        "code" -> error.code,
        "message" -> error.message
      )

  implicit val unknownServerErrorReads: Reads[UnknownServerError] =
    (json: JsValue) => {
      (json \ "code").asOpt[String] match {
        case Some("UNKNOWN_ERROR") =>
          for {
            message <- (json \ "message").validate[String]
          } yield UnknownServerError(message)
        case _ =>
          JsError("Unknown or missing error code")
      }
    }
}
