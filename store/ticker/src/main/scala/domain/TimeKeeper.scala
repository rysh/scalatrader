package domain

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TimeKeeper(
  /** 間隔(分) */
  interval: Int,
  /** 基準時間 */
  baseTime: LocalDateTime = LocalDateTime.now()
) {
  println(s"basetime : ${baseTime}")
  /** 時間切れになる時間 */
  val target = baseTime.plus(interval, ChronoUnit.MINUTES).withSecond(0).withNano(0).minus(1, ChronoUnit.NANOS)

  def lap(time: LocalDateTime): _root_.domain.TimeKeeper = new TimeKeeper(interval, time)

  def isElapsed(currentTime: LocalDateTime): Boolean = target.isBefore(currentTime)


  def nowElapsed: Boolean =  isElapsed(LocalDateTime.now())
  def next:TimeKeeper = lap(LocalDateTime.now())
  def time = baseTime
}
