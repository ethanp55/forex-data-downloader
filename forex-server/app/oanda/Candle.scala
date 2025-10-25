package oanda

import play.api.libs.json.{JsObject, JsResult, JsValue, Json, OFormat}
import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}

/** Case class used to store candle data returned from Oanda.
  *
  * @param complete
  *   Represents whether the candle is finished. Since we are downloading
  *   historical data, this will always be true.
  * @param volume
  *   Candle volume.
  * @param time
  *   LocalDateTime that represents when the candle finished.
  * @param bid
  *   Information about the bid price (a custom Price case class is used--see
  *   below).
  * @param mid
  *   Information about the mid price (a custom Price case class is used--see
  *   below).
  * @param ask
  *   Information about the ask price (a custom Price case class is used--see
  *   below).
  */
case class Candle(
    complete: Boolean,
    volume: Int,
    time: LocalDateTime,
    bid: Option[Price],
    mid: Option[Price],
    ask: Option[Price]
)

/** Companion Candle object used for defining JSON functionality.
  */
object Candle {
  implicit val candleFormat: OFormat[Candle] = new OFormat[Candle] {
    override def writes(o: Candle): JsObject = {
      val baseJson = Json.obj(
        "complete" -> o.complete,
        "volume" -> o.volume,
        "time" -> o.time.toEpochSecond(ZoneOffset.UTC).toString
      )

      val optionalFields = Seq(
        o.bid.map(bid => Json.obj("bid" -> Price.priceFormat.writes(bid))),
        o.mid.map(mid => Json.obj("mid" -> Price.priceFormat.writes(mid))),
        o.ask.map(ask => Json.obj("ask" -> Price.priceFormat.writes(ask)))
      ).flatten
      val optionalJson = optionalFields.foldLeft(Json.obj())(_ ++ _)

      baseJson ++ optionalJson
    }

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

/** Case class used to store price information. For bid, mid, and ask prices,
  * each one contains information about the open, high, low, and close for a
  * candle.
  * @param o
  *   Open price.
  * @param h
  *   High price.
  * @param l
  *   Low price.
  * @param c
  *   Close price.
  */
case class Price(o: Float, h: Float, l: Float, c: Float)

/** Companion Price object used for defining JSON functionality.
  */
object Price {
  implicit val priceFormat: OFormat[Price] = new OFormat[Price] {
    override def writes(o: Price): JsObject = Json.obj(
      "o" -> o.o.toString,
      "h" -> o.h.toString,
      "l" -> o.l.toString,
      "c" -> o.c.toString
    )

    override def reads(json: JsValue): JsResult[Price] = for {
      o <- (json \ "o").validate[String].map(_.toFloat)
      h <- (json \ "h").validate[String].map(_.toFloat)
      l <- (json \ "l").validate[String].map(_.toFloat)
      c <- (json \ "c").validate[String].map(_.toFloat)
    } yield Price(o, h, l, c)
  }
}
