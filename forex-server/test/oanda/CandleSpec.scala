package oanda

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.*
import java.time.{LocalDateTime, ZoneId}

class CandleSpec extends AnyFunSpec with Matchers {
  describe("Candle format") {
    val fixedTimestamp = 1634731200L
    val fixedLocalDateTime = LocalDateTime.ofInstant(
      java.time.Instant.ofEpochSecond(fixedTimestamp),
      ZoneId.of("UTC")
    )
    val bid =
      Some(Price(o = 1.23456f, h = 1.23500f, l = 1.23400f, c = 1.23480f))
    val mid =
      Some(Price(o = 2.23456f, h = 2.23500f, l = 2.23400f, c = 2.23480f))
    val ask =
      Some(Price(o = 3.23456f, h = 3.23500f, l = 3.23400f, c = 3.23480f))

    it("should handle round-trip serialization and deserialization") {
      val candle = Candle(true, 1000, fixedLocalDateTime, bid, mid, ask)
      val toJson = Json.toJson(candle)
      val fromJson = toJson.validate[Candle]

      fromJson shouldBe JsSuccess(candle, __)
    }

    describe("with missing prices") {
      val candle = Candle(true, 1000, fixedLocalDateTime, None, None, None)
      val candleJson = Json.obj(
        "complete" -> true,
        "volume" -> 1000,
        "time" -> "1634731200"
      )

      it("should remove prices that are not present when serializing") {
        val toJson = Json.toJson(candle)

        toJson shouldEqual candleJson
      }

      it("should remove prices that are not present when deserializing") {
        val fromJson = candleJson.validate[Candle]

        fromJson shouldBe JsSuccess(candle, __)
        val deserializedCandle = fromJson.get
        deserializedCandle.bid shouldBe None
        deserializedCandle.mid shouldBe None
        deserializedCandle.ask shouldBe None
      }
    }
  }

  describe("Price format") {
    val price = Price(o = 1.23456f, h = 1.23500f, l = 1.23400f, c = 1.23480f)
    val priceJson = Json.obj(
      "o" -> "1.23456",
      "h" -> "1.235",
      "l" -> "1.234",
      "c" -> "1.2348"
    )

    it("should correctly serialize a Price object to JSON") {
      val writtenJson = Json.toJson(price)

      writtenJson shouldEqual priceJson
    }

    it("should correctly deserialize a Price object from JSON") {
      val readResult = priceJson.validate[Price]

      readResult shouldBe JsSuccess(price, __)
      readResult.get shouldEqual price
    }

    it("should handle round-trip serialization and deserialization") {
      val toJson = Json.toJson(price)
      val fromJson = toJson.validate[Price]

      fromJson shouldBe JsSuccess(price, __)
    }
  }
}
