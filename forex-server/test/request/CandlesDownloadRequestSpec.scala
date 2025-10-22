package request

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

class CandlesDownloadRequestSpec extends AnyFunSpec with Matchers {
  describe("CandlesDownloadRequest format") {
    it("should serialize to and deserialize from JSON correctly") {
      val candlesDownloadRequest = CandlesDownloadRequest(
        EUR_USD,
        H6,
        Seq(Bid),
        "2024-01-01 00:00:00",
        "2025-01-01 00:00:00"
      )
      val expectedJson = Json.obj(
        "currencyPair" -> "EUR_USD",
        "granularity" -> "H6",
        "pricingComponents" -> Json.arr("B"),
        "fromDate" -> "2024-01-01 00:00:00",
        "toDate" -> "2025-01-01 00:00:00"
      )

      // Serialize
      val json = Json.toJson(candlesDownloadRequest)
      json shouldBe expectedJson

      // Deserialize
      val deserialized = json.validate[CandlesDownloadRequest]
      deserialized.isSuccess shouldBe true
      deserialized.get shouldBe candlesDownloadRequest
    }

    it(
      "should properly serialize and deserialize sequences of price components"
    ) {
      val candlesDownloadRequest = CandlesDownloadRequest(
        GBP_JPY,
        M15,
        Seq(Ask, Mid),
        "2024-01-01 00:00:00",
        "2025-01-01 00:00:00"
      )
      val expectedJson = Json.obj(
        "currencyPair" -> "GBP_JPY",
        "granularity" -> "M15",
        "pricingComponents" -> Json.arr("A", "M"),
        "fromDate" -> "2024-01-01 00:00:00",
        "toDate" -> "2025-01-01 00:00:00"
      )

      // Serialize
      val json = Json.toJson(candlesDownloadRequest)
      json shouldBe expectedJson

      // Deserialize
      val deserialized = json.validate[CandlesDownloadRequest]
      deserialized.isSuccess shouldBe true
      deserialized.get shouldBe candlesDownloadRequest
    }
  }
}
