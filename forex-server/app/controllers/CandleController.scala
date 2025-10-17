package controllers

import java.util.concurrent.TimeoutException
import javax.inject.*
import play.api.*
import play.api.mvc.*
import _root_.request.CandlesDownloadRequest
import oanda.{DataDownloader, OandaApiError}
import play.api.libs.json.Json
import sttp.model.StatusCode

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CandleController @Inject() (
    val controllerComponents: ControllerComponents
)(implicit
    ec: ExecutionContext
) extends BaseController {

  def downloadCandles() = Action.async(parse.json) { request =>
    request.body
      .validate[CandlesDownloadRequest]
      .fold(
        errors => {
          Future.successful(BadRequest("Invalid request format"))
        },
        candlesDownloadRequest => {
          val candlesFuture =
            DataDownloader.downloadCandles(candlesDownloadRequest)
          candlesFuture
            .map {
              case Left(error)    => BadRequest(Json.toJson(error))
              case Right(candles) => Ok(Json.toJson(candles))
            }
            .recover { case e: TimeoutException =>
              RequestTimeout(
                Json.toJson(
                  OandaApiError(
                    s"Timeout to Oanda API call with request $candlesDownloadRequest",
                    StatusCode.RequestTimeout.toString
                  )
                )
              )
            }
        }
      )
  }
}
