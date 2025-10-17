package request

import play.api.libs.json._

sealed trait Granularity {
  def toMinutes: Int
}

object Granularity {
  implicit val granularityFormat: Format[Granularity] =
    new Format[Granularity] {
      def writes(o: Granularity): JsValue = o match {
        case M1  => JsString("M1")
        case M2  => JsString("M2")
        case M4  => JsString("M4")
        case M5  => JsString("M5")
        case M10 => JsString("M10")
        case M15 => JsString("M15")
        case M30 => JsString("M30")
        case H1  => JsString("H1")
        case H2  => JsString("H2")
        case H3  => JsString("H3")
        case H4  => JsString("H4")
        case H6  => JsString("H6")
        case H8  => JsString("H8")
        case H12 => JsString("H12")
        case D   => JsString("D")
        case W   => JsString("W")
        case M   => JsString("M")
      }

      def reads(json: JsValue): JsResult[Granularity] = json match {
        case JsString("M1")  => JsSuccess(M1)
        case JsString("M2")  => JsSuccess(M2)
        case JsString("M4")  => JsSuccess(M4)
        case JsString("M5")  => JsSuccess(M5)
        case JsString("M10") => JsSuccess(M10)
        case JsString("M15") => JsSuccess(M15)
        case JsString("M30") => JsSuccess(M30)
        case JsString("H1")  => JsSuccess(H1)
        case JsString("H2")  => JsSuccess(H2)
        case JsString("H3")  => JsSuccess(H3)
        case JsString("H4")  => JsSuccess(H4)
        case JsString("H6")  => JsSuccess(H6)
        case JsString("H8")  => JsSuccess(H8)
        case JsString("H12") => JsSuccess(H12)
        case JsString("D")   => JsSuccess(D)
        case JsString("W")   => JsSuccess(W)
        case JsString("M")   => JsSuccess(M)
        case _               => JsError("Unknown Granularity")
      }
    }
}

case object M1 extends Granularity {
  override def toMinutes: Int = 1
}

case object M2 extends Granularity {
  override def toMinutes: Int = 2
}

case object M4 extends Granularity {
  override def toMinutes: Int = 4
}

case object M5 extends Granularity {
  override def toMinutes: Int = 5
}

case object M10 extends Granularity {
  override def toMinutes: Int = 10
}

case object M15 extends Granularity {
  override def toMinutes: Int = 15
}

case object M30 extends Granularity {
  override def toMinutes: Int = 30
}

case object H1 extends Granularity {
  override def toMinutes: Int = 60
}

case object H2 extends Granularity {
  override def toMinutes: Int = 60 * 2
}

case object H3 extends Granularity {
  override def toMinutes: Int = 60 * 3
}

case object H4 extends Granularity {
  override def toMinutes: Int = 60 * 4
}

case object H6 extends Granularity {
  override def toMinutes: Int = 60 * 6
}

case object H8 extends Granularity {
  override def toMinutes: Int = 60 * 8
}

case object H12 extends Granularity {
  override def toMinutes: Int = 60 * 12
}

case object D extends Granularity {
  override def toMinutes: Int = 60 * 24
}

case object W extends Granularity {
  override def toMinutes: Int = 60 * 24 * 7
}

case object M extends Granularity {
  // 30 days as an approximation
  override def toMinutes: Int = 60 * 24 * 30
}
