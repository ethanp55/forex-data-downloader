package controllers

import javax.inject.*
import play.api.*
import play.api.mvc.*
import _root_.request.CandlesDownloadRequest
import oanda.{
  Candle,
  DataDownloader,
  OandaApiServerError,
  ServerError,
  UnknownServerError
}
import org.apache.pekko.actor.ActorSystem
import play.api.libs.json.Json
import sttp.model.StatusCode
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import org.apache.pekko.pattern.after

@Singleton
class CandleController @Inject() (
    val controllerComponents: ControllerComponents,
    actorSystem: ActorSystem
)(implicit
    ec: ExecutionContext
) extends BaseController {

  private val timeoutDuration = 20.seconds

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
          val timeoutFuture = after(
            timeoutDuration,
            using = actorSystem.scheduler
          )(Future {
            Left(
              OandaApiServerError(
                s"Timeout to Oanda API call with request $candlesDownloadRequest",
                StatusCode.RequestTimeout.toString
              )
            )
          })
          val resultWithTimeout =
            Future.firstCompletedOf(Seq(candlesFuture, timeoutFuture))

          resultWithTimeout
            .map {
              case Left(error)    => BadRequest(Json.toJson(error))
              case Right(candles) => Ok(Json.toJson(candles))
            }
            .recover { case exception: Exception =>
              InternalServerError(
                Json.toJson(
                  UnknownServerError(
                    s"Timeout to Oanda API call with request $candlesDownloadRequest"
                  )
                )
              )
            }
        }
      )
  }
}
