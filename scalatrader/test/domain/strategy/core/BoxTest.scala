package domain.strategy.core

import java.time.ZonedDateTime

import domain.models.Ticker
import domain.time.DateUtil
import org.scalatest.FunSuite

class BoxTest extends FunSuite {

  test("testIsUpdatingHigh") {
    val box = new Box(10, 10)
    val highTicker = newTicker("2017-12-08T10:10:00Z", 100)
    box.put(highTicker)

//    assert(box.isUpdatingHigh(ZonedDateTime.parse("2017-12-08T10:10:00Z"), 10) === true)
    assert(box.isUpdatingHigh(ZonedDateTime.parse("2017-12-08T10:20:00Z"), 11 * 60) === true)
  }

  test("bulk create") {
    val bar1 = new Bar(20180101000000L)
    bar1.put(newTicker(toTimestamp(20180101000001L), 100))
    bar1.put(newTicker(toTimestamp(20180101000002L), 150))
    bar1.put(newTicker(toTimestamp(20180101000003L), 200))
    val bar2 = new Bar(20180101000111L)
    bar2.put(newTicker(toTimestamp(20180101000111L), 300))
    bar2.put(newTicker(toTimestamp(20180101000112L), 450))
    bar2.put(newTicker(toTimestamp(20180101000113L), 250))

    val box = Box.of(List(bar1, bar2).toSeq, 10)
    assert(box.open === 100.0)
    assert(box.close === 250.0)
    assert(box.high === 450.0)
    assert(box.low === 100.0)
    assert(box.isUp === true)
  }

  private def toTimestamp(key: Long): String = DateUtil.parseKey(key).toOffsetDateTime.toString
  def newTicker(timestamp: String, ltp: Double) = Ticker("", timestamp, 0, 0, 0, 0, 0, 0, 0, ltp, 0, 0)
}
