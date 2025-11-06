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

/** Class that handles requests to download historical candle data from Oanda.
  * The @Singleton decorator is used because only one instance is needed.
  * @param controllerComponents
  *   Default ControllerComponents provided by the Play framework.
  * @param actorSystem
  *   Default Pekko ActorSystem provided by the Play framework.
  * @param candleDownloader
  *   Singleton CandleDownloader instance (defined in CandleDownloader.scala)
  *   used to send requests to Oanda and process any returned results.
  * @param ec
  *   Default ExecutionContext provided by Play (for handling multiple requests
  *   at a time).
  */
@Singleton
class CandleController @Inject() (
    val controllerComponents: ControllerComponents,
    actorSystem: ActorSystem,
    candleDownloader: CandleDownloader
)(implicit
    ec: ExecutionContext
) extends BaseController {

  protected val timeoutDuration: FiniteDuration = 20.seconds

  /** The main (and only) endpoint for downloading historical candle data from
    * Oanda. See the routes file. Note that a POST request must be made (not a
    * GET request) because the endpoint is expecting a JSON body representing a
    * CandlesDownloadRequest (see CandlesDownloadRequest.scala). Action.async is
    * used in order to be able to process multiple requests to the endpoint at a
    * time (i.e., provide non-blocking functionality).
    * @return
    *   A Future result. If everything works properly, a JSONified sequence of
    *   Candle objects is returned in an Ok response.
    */
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
              case Right(candles) =>
                try {
                  Ok(Json.toJson(candles))
                } catch {
                  case e: Exception =>
                    InternalServerError(
                      Json.toJson(
                        UnknownServerError(
                          s"Could not convert candles to JSON for request $candlesDownloadRequest: ${e.getMessage}"
                        )
                      )
                    )
                }
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
