package request

import play.api.libs.json._

/** Sealed trait for defining PricingComponent enums.
  */
sealed trait PricingComponent

/** Companion PricingComponent object used for defining JSON functionality.
  */
object PricingComponent {
  implicit val pricingComponentFormat: Format[PricingComponent] =
    new Format[PricingComponent] {
      def writes(o: PricingComponent): JsValue = o match {
        case Bid => JsString("B")
        case Mid => JsString("M")
        case Ask => JsString("A")
      }

      def reads(json: JsValue): JsResult[PricingComponent] = json match {
        case JsString("B") => JsSuccess(Bid)
        case JsString("M") => JsSuccess(Mid)
        case JsString("A") => JsSuccess(Ask)
        case _             => JsError("Unknown PricingComponent")
      }
    }

  def combine(pricingComponents: Seq[PricingComponent]): String = {
    pricingComponents.toSet.map(_.toString).mkString("")
  }
}

/** Case objects for the different prices users can request. Note that toString
  * is overridden instead of naming each B, M, and A, respectively, because the
  * case object M is already defined in Granularity.scala, plus Bid, Mid, and
  * Ask are more understandable/intuitive names.
  */
case object Bid extends PricingComponent {
  override def toString: String = "B"
}

case object Mid extends PricingComponent {
  override def toString: String = "M"
}

case object Ask extends PricingComponent {
  override def toString: String = "A"
}
