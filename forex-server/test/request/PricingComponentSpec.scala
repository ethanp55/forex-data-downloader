package request

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

class PricingComponentSpec extends AnyFunSpec with Matchers {
  private val pricingComponents: Seq[PricingComponent] = Seq(Bid, Mid, Ask)

  describe("PricingComponent format") {
    val expectedJsons = pricingComponents.map(pricingComponent =>
      Json.toJson(pricingComponent.toString)
    )

    pricingComponents.zip(expectedJsons).foreach {
      (pricingComponent, expectedJson) =>
        it(
          s"should serialize to and deserialize from JSON correctly for $pricingComponent"
        ) {
          // Serialize
          val json = Json.toJson(pricingComponent)
          json shouldBe expectedJson

          // Deserialize
          val deserialized = json.validate[PricingComponent]
          deserialized.isSuccess shouldBe true
          deserialized.get shouldBe pricingComponent
        }
    }
  }

  describe("PricingComponent combine") {
    it("should properly combine one pricing component") {
      PricingComponent.combine(Seq(Bid)) shouldEqual "B"
      PricingComponent.combine(Seq(Mid)) shouldEqual "M"
      PricingComponent.combine(Seq(Ask)) shouldEqual "A"
    }

    it("should properly combine two pricing components") {
      PricingComponent.combine(Seq(Bid, Ask)) shouldEqual "BA"
      PricingComponent.combine(Seq(Ask, Bid)) shouldEqual "AB"
      PricingComponent.combine(Seq(Mid, Ask)) shouldEqual "MA"
      PricingComponent.combine(Seq(Ask, Mid)) shouldEqual "AM"
      PricingComponent.combine(Seq(Bid, Mid)) shouldEqual "BM"
      PricingComponent.combine(Seq(Mid, Bid)) shouldEqual "MB"
    }

    it("should properly combine three pricing components") {
      PricingComponent.combine(Seq(Bid, Mid, Ask)) shouldEqual "BMA"
      PricingComponent.combine(Seq(Bid, Ask, Mid)) shouldEqual "BAM"
      PricingComponent.combine(Seq(Ask, Mid, Bid)) shouldEqual "AMB"
      PricingComponent.combine(Seq(Ask, Bid, Mid)) shouldEqual "ABM"
      PricingComponent.combine(Seq(Mid, Bid, Ask)) shouldEqual "MBA"
      PricingComponent.combine(Seq(Mid, Ask, Bid)) shouldEqual "MAB"
    }

    it("should properly remove duplicates") {
      // Just a few arbitrary cases
      PricingComponent.combine(Seq(Bid, Bid, Ask)) shouldEqual "BA"
      PricingComponent.combine(
        Seq(Mid, Ask, Bid, Bid, Ask, Ask, Ask, Mid)
      ) shouldEqual "MAB"
      PricingComponent.combine(Seq(Ask, Ask)) shouldEqual "A"
    }
  }
}
