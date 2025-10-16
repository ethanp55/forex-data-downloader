package oanda

import play.api.libs.json.{JsObject, JsResult, JsValue, Json, OFormat}
import java.time.{Instant, LocalDateTime, ZoneId}

case class Candle(
    complete: Boolean,
    volume: Int,
    time: LocalDateTime,
    bid: Option[Price],
    mid: Option[Price],
    ask: Option[Price]
)

object Candle {
  implicit val candleFormat: OFormat[Candle] = new OFormat[Candle] {
    override def writes(o: Candle): JsObject = Json.obj(
      "complete" -> o.complete,
      "volume" -> o.volume,
      "time" -> o.time,
      "bid" -> o.bid,
      "mid" -> o.mid,
      "ask" -> o.ask
    )

    def reads(json: JsValue): JsResult[Candle] = for {
      complete <- (json \ "complete").validate[Boolean]
      volume <- (json \ "volume").validate[Int]
      time <- (json \ "time").validate[String].map { x =>
        val instant = Instant.ofEpochSecond(x.toDouble.toLong)
        LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
      }
      bid <- (json \ "bid").validateOpt[Price]
      mid <- (json \ "mid").validateOpt[Price]
      ask <- (json \ "ask").validateOpt[Price]
    } yield Candle(complete, volume, time, bid, mid, ask)
  }
}

case class Price(o: Float, h: Float, l: Float, c: Float)

object Price {
  implicit val priceFormat: OFormat[Price] = new OFormat[Price] {
    override def writes(o: Price): JsObject = Json.obj(
      "o" -> o.o,
      "h" -> o.h,
      "l" -> o.l,
      "c" -> o.c
    )

    override def reads(json: JsValue): JsResult[Price] = for {
      o <- (json \ "o").validate[String].map(_.toFloat)
      h <- (json \ "h").validate[String].map(_.toFloat)
      l <- (json \ "l").validate[String].map(_.toFloat)
      c <- (json \ "c").validate[String].map(_.toFloat)
    } yield Price(o, h, l, c)
  }
}
