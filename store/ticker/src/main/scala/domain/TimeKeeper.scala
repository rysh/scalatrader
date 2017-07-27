package domain

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{ZoneId, ZonedDateTime}
import domain.TimeKeeper._

class TimeKeeper(
  /** 間隔(分) */
  interval: Int,
  /** 基準時間 */
  baseTime: ZonedDateTime = now()
) {
  def format(pattern: String): String = time.format(DateTimeFormatter.ofPattern(pattern))

  /** 時間切れになる時間 */
  val target = baseTime.plus(interval, ChronoUnit.MINUTES).withSecond(0).withNano(0).minus(1, ChronoUnit.NANOS)

  def lap(time: ZonedDateTime): _root_.domain.TimeKeeper = new TimeKeeper(interval, time)

  def isElapsed(currentTime: ZonedDateTime): Boolean = target.isBefore(currentTime)
//    {
//      println(
//        s"""
//          |${target}
//          |${currentTime}
//          |${target.isBefore(currentTime)}
//        """.stripMargin)
//      target.isBefore(currentTime)
//    }


  def nowElapsed: Boolean =  isElapsed(now())
  def next:TimeKeeper = lap(now())
  def time = baseTime
}

object TimeKeeper {
  def now(): ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))

}