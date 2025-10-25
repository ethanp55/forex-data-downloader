package oanda

import play.api.libs.json.{JsError, JsValue, Json, Reads, Writes}

/** Simple trait for defining custom errors to send back to the user (to help
  * clarify what, if anything, went wrong).
  */
sealed trait ServerError {
  val code: String
  val message: String
}

/** Companion ServerError object used for defining JSON functionality.
  */
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

/** Case class that implements the ServerError trait. Used for sending error
  * messages if any time conversions to UNIX fail.
  * @param message
  *   Custom error message to help indicate what went wrong.
  */
case class DateTimeParseServerError(message: String) extends ServerError {
  override val code = "INVALID_DATE_STRING"
}

/** Companion DateTimeParseServerError object used for defining JSON
  * functionality.
  */
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

/** Case class that implements the ServerError trait. Used for sending error
  * messages if any requests to Oanda error out.
  * @param message
  *   Custom error message to help indicate what went wrong.
  */
case class OandaApiServerError(message: String) extends ServerError {
  override val code = "OANDA_API_ERROR"
}

/** Companion OandaApiServerError object used for defining JSON functionality.
  */
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

/** Case class that implements the ServerError trait. Used for sending error
  * messages if any strange/unknown errors occur.
  * @param message
  *   Custom error message to help indicate what went wrong.
  */
case class UnknownServerError(message: String) extends ServerError {
  override val code = "UNKNOWN_ERROR"
}

/** Companion UnknownServerError object used for defining JSON functionality.
  */
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
