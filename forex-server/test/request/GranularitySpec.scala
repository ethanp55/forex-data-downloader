package request

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

class GranularitySpec extends AnyFunSpec with Matchers {
  private val granularities: Seq[Granularity] =
    Seq(M1, M2, M4, M5, M10, M15, M30, H1, H2, H3, H4, H6, H8, H12, D, W, M)

  describe("Granularity format") {
    val expectedJsons =
      granularities.map(granularity => Json.toJson(granularity.toString))

    granularities.zip(expectedJsons).foreach { (granularity, expectedJson) =>
      it(
        s"should serialize to and deserialize from JSON correctly for $granularity"
      ) {
        // Serialize
        val json = Json.toJson(granularity)
        json shouldBe expectedJson

        // Deserialize
        val deserialized = json.validate[Granularity]
        deserialized.isSuccess shouldBe true
        deserialized.get shouldBe granularity
      }
    }
  }

  describe("Granularity minutes") {
    val expectedMinutes = Seq(1, 2, 4, 5, 10, 15, 30, 60, 120, 180, 240, 360,
      480, 720, 1440, 10080, 43200)

    granularities.zip(expectedMinutes).foreach {
      (granularity, expectedMinute) =>
        it(s"should be calculated properly for $granularity") {
          granularity.toMinutes shouldEqual expectedMinute
        }
    }
  }
}
