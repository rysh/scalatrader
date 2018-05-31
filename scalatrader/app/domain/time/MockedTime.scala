package domain.time

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object MockedTime {

  var now: ZonedDateTime = DateUtil.now()

  def start(start: ZonedDateTime): Unit = {
    MockedTime.now = start
  }

  def isFinished(end: ZonedDateTime): Boolean = MockedTime.now.isBefore(end)

  def add1Minutes(): Unit = {
    MockedTime.now = MockedTime.now.plus(1, ChronoUnit.MINUTES)
  }
}
