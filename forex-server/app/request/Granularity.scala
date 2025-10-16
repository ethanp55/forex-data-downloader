package request

sealed trait Granularity {
  def toMinutes: Int
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
