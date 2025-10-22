package oanda

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.*

class ServerErrorSpec extends AnyFunSpec with Matchers {
  describe("DateTimeParseServerError") {
    it("should serialize and deserialize correctly") {
      val error = DateTimeParseServerError("Invalid date format")
      val json = Json.toJson(error)
      val expectedJson = Json.obj(
        "message" -> "Invalid date format",
        "code" -> "INVALID_DATE_STRING"
      )

      json shouldEqual expectedJson

      val deserialized = json.validate[DateTimeParseServerError]
      deserialized.isSuccess shouldBe true
      deserialized.get shouldBe error
    }
  }

  describe("OandaApiServerError") {
    it("should serialize and deserialize correctly") {
      val error = OandaApiServerError("API is down")
      val json = Json.toJson(error)
      val expectedJson = Json.obj(
        "message" -> "API is down",
        "code" -> "OANDA_API_ERROR"
      )

      json shouldEqual expectedJson

      val deserialized = json.validate[OandaApiServerError]
      deserialized.isSuccess shouldBe true
      deserialized.get shouldBe error
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

      val deserialized = json.validate[UnknownServerError]
      deserialized.isSuccess shouldBe true
      deserialized.get shouldBe error
    }
  }
}
