package request

sealed trait PricingComponent

object PricingComponent {
  def combine(pricingComponents: Seq[PricingComponent]): String = {
    pricingComponents.map(_.toString).mkString("")
  }
}

// Chose to override toString instead of naming each B, M, and A, respectively, because the case object M is already
// defined in Granularity.scala, plus Bid, Mid, and Ask are more understandable names
case object Bid extends PricingComponent {
  override def toString: String = "B"
}

case object Mid extends PricingComponent {
  override def toString: String = "M"
}

case object Ask extends PricingComponent {
  override def toString: String = "A"
}
