package domain.time

import java.time.temporal.ChronoUnit
import java.time.{ZonedDateTime, ZoneId}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

object ScheduleHelper {

  def initialDelay(interval: FiniteDuration): FiniteDuration = {
    initialDelay(interval, DateUtil.now())
  }

  def initialDelay(interval: FiniteDuration, dateTime: ZonedDateTime): FiniteDuration = {
    var temp = dateTime.withHour(0).withMinute(0).withSecond(0).withNano(0)
    while (temp.isBefore(dateTime)) {
      temp = temp.plusSeconds(interval.toSeconds)
    }
    ChronoUnit.MILLIS.between(dateTime, temp).milliseconds
  }

}
