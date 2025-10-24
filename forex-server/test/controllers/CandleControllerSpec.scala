package controllers

import oanda.{
  Candle,
  CandleDownloader,
  OandaApiServerError,
  Price,
  UnknownServerError
}
import org.apache.pekko.actor.ActorSystem
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsString, JsValue, Json}
import play.api.test.*
import play.api.test.Helpers.*
import request.{Ask, Bid, CandlesDownloadRequest, D, USD_JPY}
import java.time.{LocalDateTime, ZoneId}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import org.apache.pekko.pattern.{after => PekkoAfter}

class CandleControllerSpec
    extends AsyncFunSpec
    with Matchers
    with MockitoSugar {
  private implicit val actorSystemMock: ActorSystem = ActorSystem("test")
  private implicit val ec: ExecutionContext = actorSystemMock.getDispatcher
  private val candleDownloaderMock = mock[CandleDownloader]
  private val controllerComponents = stubControllerComponents()
  private val candleControllerMock = new CandleController(
    controllerComponents,
    actorSystemMock,
    candleDownloaderMock
  )

  private val candlesDownloadRequest = CandlesDownloadRequest(
    USD_JPY,
    D,
    Seq(Bid, Ask),
    "2024-01-01 00:00:00",
    "2025-01-01 00:00:00"
  )

  describe("CandleController") {
    describe("downloadCandles endpoint") {
      it(
        "should return an UnsupportedMediaType response for improper request content type"
      ) {
        val request = FakeRequest(POST, "/downloadCandles")
        val response = candleControllerMock
          .downloadCandles()
          .apply(request)

        status(response) shouldBe UNSUPPORTED_MEDIA_TYPE
      }

      it("should return a BadRequest response for improper request bodies") {
        val request = FakeRequest(POST, "/downloadCandles")
          .withHeaders(
            "Content-Type" -> "application/json"
          )
          .withBody(Json.obj("foo" -> "bar"))
        val response = candleControllerMock
          .downloadCandles()
          .apply(request)

        status(response) shouldBe BAD_REQUEST
        contentAsString(response) shouldBe "Invalid request format"
      }

      it("should return an Ok response for proper request bodies") {
        // Request
        val request = FakeRequest(POST, "/downloadCandles")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(candlesDownloadRequest))

        // Expected response
        val fixedTimestamp = 1634731200L
        val fixedLocalDateTime = LocalDateTime.ofInstant(
          java.time.Instant.ofEpochSecond(fixedTimestamp),
          ZoneId.of("UTC")
        )
        val bid =
          Some(Price(o = 1.23456f, h = 1.23500f, l = 1.23400f, c = 1.23480f))
        val ask =
          Some(Price(o = 3.23456f, h = 3.23500f, l = 3.23400f, c = 3.23480f))
        val candles =
          Seq(Candle(true, 1000, fixedLocalDateTime, bid, None, ask))

        // Set the expected response
        when(
          candleDownloaderMock.downloadCandles(any[CandlesDownloadRequest])(
            any[ActorSystem]
          )
        )
          .thenReturn(Future.successful(Right(candles)))

        // Test response
        val response = candleControllerMock.downloadCandles().apply(request)

        status(response) shouldBe OK
        contentAsJson(response) shouldBe Json.toJson(candles)
      }

      it("should time out if the Oanda API takes too long to response") {
        // Request
        val request = FakeRequest(POST, "/downloadCandles")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(candlesDownloadRequest))

        // Controller that instantly creates a timeout
        val candleController = new CandleController(
          controllerComponents,
          actorSystemMock,
          candleDownloaderMock
        )(actorSystemMock.getDispatcher) {
          override protected val timeoutDuration: FiniteDuration = 0.seconds
        }

        // Set the candle downloader to take a long time to finish (60 seconds should be plenty large)
        when(
          candleDownloaderMock.downloadCandles(any[CandlesDownloadRequest])(
            any[ActorSystem]
          )
        ).thenReturn(
          PekkoAfter(60.seconds, using = actorSystemMock.scheduler)(
            Future.successful(Left(OandaApiServerError("foo")))
          )
        )

        // Test response
        val response = candleController.downloadCandles().apply(request)

        status(response) shouldBe BAD_REQUEST
        val jsonResult = contentAsJson(response)
        (jsonResult \ "code")
          .getOrElse(Json.toJson("wah wah"))
          .asInstanceOf[JsString]
          .value shouldBe "OANDA_API_ERROR"
        (jsonResult \ "message")
          .getOrElse(Json.toJson("wah wah"))
          .asInstanceOf[JsString]
          .value shouldBe s"Timeout to Oanda API call with request $candlesDownloadRequest"
      }

      it(
        "should return an error if the candle downloader finishes quickly but with an error"
      ) {
        // Request
        val request = FakeRequest(POST, "/downloadCandles")
          .withHeaders("Content-Type" -> "application/json")
          .withBody(Json.toJson(candlesDownloadRequest))

        // Set the candle downloader to return an error
        when(
          candleDownloaderMock.downloadCandles(any[CandlesDownloadRequest])(
            any[ActorSystem]
          )
        )
          .thenReturn(Future.successful(Left(UnknownServerError("woopsie"))))

        // Test response
        val response = candleControllerMock.downloadCandles().apply(request)

        status(response) shouldBe BAD_REQUEST
        val jsonResult = contentAsJson(response)
        (jsonResult \ "code")
          .getOrElse(Json.toJson("wah wah"))
          .asInstanceOf[JsString]
          .value shouldBe "UNKNOWN_ERROR"
        (jsonResult \ "message")
          .getOrElse(Json.toJson("wah wah"))
          .asInstanceOf[JsString]
          .value shouldBe "woopsie"
      }
    }
  }
}
