package oanda

case class Candle(complete: Boolean,
                  volume: Int,
                  time: Double,
                  bid: Option[Price],
                  mid: Option[Price],
                  ask: Option[Price])

case class Price(o: Double, h: Double, l: Double, c: Double)
