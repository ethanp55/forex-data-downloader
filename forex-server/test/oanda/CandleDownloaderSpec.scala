package oanda

import java.time.{LocalDateTime, ZoneOffset}
import org.mockito.Mockito.when
import oanda.{
  Candle,
  CandleDownloader,
  DateTimeParseServerError,
  OandaApiServerError,
  Price,
  ServerError
}
import org.apache.pekko.actor.ActorSystem
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import request.{Bid, CandlesDownloadRequest, EUR_USD, H1}
import sttp.client4.{Request, Response, SyncBackend}

import scala.concurrent.{ExecutionContext, Future}

class CandleDownloaderTest(backend: SyncBackend)
    extends CandleDownloader(backend) {
  def dateTimeToUnixPublic(localDateTime: LocalDateTime): String =
    super.dateTimeToUnix(localDateTime)

  def createRequestsPublic(
      candlesDownloadRequest: CandlesDownloadRequest
  ): Either[ServerError, Seq[Request[String]]] =
    super.createRequests(candlesDownloadRequest)

  def sendRequestsPublic(
      requests: Seq[Request[String]]
  )(implicit
      executionContext: ExecutionContext,
      actorSystem: ActorSystem
  ): Future[Either[ServerError, Seq[Candle]]] = super.sendRequests(requests)
}

class CandleDownloaderSpec
    extends AsyncFunSpec
    with Matchers
    with MockitoSugar {
  private val mockBackend = mock[SyncBackend]
  private val mockRequest = mock[Request[String]]
  private val mockResponse = mock[Response[String]]
  private val candleDownloaderTest: CandleDownloaderTest =
    new CandleDownloaderTest(mockBackend)
  private implicit val mockActorSystem: ActorSystem = mock[ActorSystem]

  describe("CandleDownloader") {
    describe("dateTimeToUnix") {
      it("should correctly convert LocalDateTime to Unix timestamp") {
        // Test case
        val testDateTime =
          LocalDateTime.of(2025, 1, 1, 0, 0, 0, 0) // 2021-01-01 00:00:00
        val result = candleDownloaderTest.dateTimeToUnixPublic(testDateTime)

        // Expected result(s)
        result shouldEqual testDateTime.toEpochSecond(ZoneOffset.UTC).toString
      }
    }

    describe("createRequests") {
      it("should generate requests correctly for valid inputs") {
        // Input request
        val candlesDownloadRequest = CandlesDownloadRequest(
          EUR_USD,
          H1,
          Seq(Bid),
          "2021-01-01 00:00:00",
          "2022-01-01 00:00:00"
        )

        // Test case
        val testRequests =
          candleDownloaderTest.createRequestsPublic(candlesDownloadRequest)

        // Expected result(s)
        testRequests shouldBe a[Right[_, _]]
        // Using H1, we should be able to pull about 208 days of data into one request, which means we need a second to
        // cover a full year
        testRequests.getOrElse(Nil) should have length 2
      }

      it("should return an error if date parsing fails") {
        val candlesDownloadRequest = CandlesDownloadRequest(
          EUR_USD,
          H1,
          Seq(Bid),
          "foo",
          "2022-01-01 00:00:00"
        )

        // Test case
        val testRequests =
          candleDownloaderTest.createRequestsPublic(candlesDownloadRequest)

        // Expected result(s)
        testRequests shouldBe a[Left[_, _]]
        val expectedError = DateTimeParseServerError(
          "Could not convert foo and/or 2022-01-01 00:00:00; expected format = yyyy-MM-dd HH:mm:ss"
        )
        testRequests.left.getOrElse(
          DateTimeParseServerError("wah wah")
        ) shouldEqual expectedError
      }
    }

    describe("sendRequests") {
      it("should return an error for non-retry API errors from Oanda") {
        // Mock an error (that doesn't cause the candle downloader to retry any requests to Oanda)
        when(mockRequest.send(mockBackend)).thenReturn(mockResponse)
        when(mockBackend.send(mockRequest)).thenReturn(mockResponse)
        when(mockResponse.code).thenReturn(501)

        // Test case
        val result = candleDownloaderTest.sendRequestsPublic(Seq(mockRequest))

        // Expected result(s)
        result.map {
          case Left(value: OandaApiServerError) =>
            value shouldBe a[OandaApiServerError]
            value.code shouldEqual "OANDA_API_ERROR"
          case Right(_) => fail("wah wah")
        }
      }

      it("should download candles correctly for valid responses") {
        // Mock a successful response
        when(mockRequest.send(mockBackend)).thenReturn(mockResponse)
        when(mockBackend.send(mockRequest)).thenReturn(mockResponse)
        when(mockResponse.code).thenReturn(200)
        val candleJson = """{
                               |  "candles" : [ {
                               |    "complete" : true,
                               |    "volume" : 1000,
                               |    "time" : "1761067220",
                               |    "mid" : {
                               |      "o" : "1",
                               |      "h" : "2",
                               |      "l" : "1.5",
                               |      "c" : "1.025"
                               |    }
                               |  },
                               |   {
                               |    "complete" : true,
                               |    "volume" : 2000,
                               |    "time" : "1761067220",
                               |    "ask" : {
                               |      "o" : "1.5",
                               |      "h" : "2.5",
                               |      "l" : "2",
                               |      "c" : "1.075"
                               |    }
                               |  }]
                               |}""".stripMargin
        when(mockResponse.body).thenReturn(candleJson)

        // Test case
        val result = candleDownloaderTest.sendRequestsPublic(Seq(mockRequest))

        // Expected result(s) -- chose a few arbitrary candle parameters to check if everything is parsed correctly
        result.map {
          case Left(_)      => fail("wah wah")
          case Right(value) =>
            value should have length 2
            val firstCandle = value.head
            val secondCandle = value(1)
            firstCandle.volume shouldEqual 1000
            firstCandle.ask shouldBe None
            firstCandle.mid.get.h shouldEqual 2
            secondCandle.complete shouldBe true
            secondCandle.mid shouldBe None
            secondCandle.ask.get.o shouldEqual 1.5
        }
      }
    }
  }
}
