package domain.time

import java.time.{ZonedDateTime, ZoneId}

import org.scalatest.FunSuite

import scala.concurrent.duration._

class InitialDelayTest extends FunSuite {

  test("interval is 10 min") {
    val datetime = ZonedDateTime.of(2017, 11, 10, 3, 59, 1, 0, ZoneId.of("UTC"))
    val delay = ScheduleHelper.initialDelay(10.minutes, datetime)
    assert(delay === 59.seconds)
  }
  test("interval is 30 sec") {
    val datetime = ZonedDateTime.of(2017, 11, 10, 3, 59, 1, 0, ZoneId.of("UTC"))
    val delay = ScheduleHelper.initialDelay(30.seconds, datetime)
    assert(delay === 29000.milliseconds)
  }
}
