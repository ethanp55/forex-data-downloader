package controllers

import javax.inject.*
import play.api.*
import play.api.mvc.*
import _root_.request.CandlesDownloadRequest
import oanda.{
  Candle,
  CandleDownloader,
  OandaApiServerError,
  ServerError,
  UnknownServerError
}
import org.apache.pekko.actor.ActorSystem
import play.api.libs.json.Json
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import org.apache.pekko.pattern.after

@Singleton
class CandleController @Inject() (
    val controllerComponents: ControllerComponents,
    actorSystem: ActorSystem,
    candleDownloader: CandleDownloader
)(implicit
    ec: ExecutionContext
) extends BaseController {

  protected val timeoutDuration: FiniteDuration = 20.seconds

  def downloadCandles() = Action.async(parse.json) { request =>
    request.body
      .validate[CandlesDownloadRequest]
      .fold(
        errors => {
          Future.successful(BadRequest("Invalid request format"))
        },
        candlesDownloadRequest => {
          val candlesFuture =
            candleDownloader
              .downloadCandles(candlesDownloadRequest)(actorSystem =
                actorSystem
              )
          val timeoutFuture = after(
            timeoutDuration,
            using = actorSystem.scheduler
          )(Future {
            Left(
              OandaApiServerError(
                s"Timeout to Oanda API call with request $candlesDownloadRequest"
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
