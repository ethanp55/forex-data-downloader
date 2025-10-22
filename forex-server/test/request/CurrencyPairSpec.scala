package request

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

class CurrencyPairSpec extends AnyFunSpec with Matchers {

  describe("CurrencyPair format") {

    val currencyPairs: Seq[CurrencyPair] = Seq(
      EUR_USD,
      USD_JPY,
      GBP_USD,
      USD_CAD,
      AUD_USD,
      USD_CHF,
      NZD_USD,
      EUR_GBP,
      EUR_JPY,
      GBP_JPY,
      GBP_CAD,
      AUD_JPY,
      EUR_AUD,
      EUR_CAD,
      EUR_CHF,
      CAD_JPY,
      GBP_CHF,
      NZD_JPY,
      CHF_JPY,
      NZD_CAD
    )
    val expectedJsons = currencyPairs.map(pair => Json.toJson(pair.toString))

    currencyPairs.zip(expectedJsons).foreach { (currencyPair, expectedJson) =>
      it(
        s"should serialize to and deserialize from JSON correctly for $currencyPair"
      ) {
        // Serialize
        val json = Json.toJson(currencyPair)
        json shouldBe expectedJson

        // Deserialize
        val deserialized = json.validate[CurrencyPair]
        deserialized.isSuccess shouldBe true
        deserialized.get shouldBe currencyPair
      }
    }
  }
}
