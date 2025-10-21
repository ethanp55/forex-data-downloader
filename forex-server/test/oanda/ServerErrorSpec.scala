package oanda

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.*

class ServerErrorSpec extends AnyFunSpec with Matchers {
  describe("DateTimeParseServerError") {
    it("serialize and deserialize correctly") {
      val error = DateTimeParseServerError("Invalid date format")
      val json = Json.toJson(error)
      val expectedJson = Json.obj(
        "message" -> "Invalid date format",
        "code" -> "INVALID_DATE_STRING"
      )

      json shouldEqual expectedJson
      Json.fromJson[DateTimeParseServerError](json) shouldBe JsSuccess(error)
    }
  }

  describe("OandaApiServerError") {
    it("serialize and deserialize correctly") {
      val error = OandaApiServerError("API is down")
      val json = Json.toJson(error)
      val expectedJson = Json.obj(
        "message" -> "API is down",
        "code" -> "OANDA_API_ERROR"
      )

      json shouldEqual expectedJson
      Json.fromJson[OandaApiServerError](json) shouldBe JsSuccess(error)
    }
  }

  describe("UnknownServerError") {
    it("should serialize and deserialize correctly") {
      val error = UnknownServerError("An unknown error occurred")
      val json = Json.toJson(error)
      val expectedJson = Json.obj(
        "message" -> "An unknown error occurred",
        "code" -> "UNKNOWN_ERROR"
      )

      json shouldEqual expectedJson
      Json.fromJson[UnknownServerError](json) shouldBe JsSuccess(error)
    }
  }
}
