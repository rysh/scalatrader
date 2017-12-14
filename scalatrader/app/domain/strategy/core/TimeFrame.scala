package domain.strategy.core

object TimeFrame {
  sealed trait TimeUnit
  case class Candle_5seconds() extends TimeUnit
  case class Candle_15seconds() extends TimeUnit
  case class Candle_30seconds() extends TimeUnit
  case class Candle_45seconds() extends TimeUnit
  case class Candle_1minutes() extends TimeUnit
  case class Candle_5minutes() extends TimeUnit
  case class Candle_15minutes() extends TimeUnit
  case class Candle_30minutes() extends TimeUnit
  case class Candle_45minutes() extends TimeUnit
  case class Candle_1hours() extends TimeUnit
  case class Candle_2hours() extends TimeUnit
  case class Candle_4hours() extends TimeUnit
  case class Candle_8hours() extends TimeUnit
  case class Candle_12hours() extends TimeUnit
  case class Candle_1days() extends TimeUnit

}
