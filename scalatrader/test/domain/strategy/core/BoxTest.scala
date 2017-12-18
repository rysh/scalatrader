package domain.strategy.core

import java.time.ZonedDateTime

import domain.models.Ticker
import org.scalatest.FunSuite

class BoxTest extends FunSuite {

  test("testIsUpdatingHigh") {
    val box = new Box(10,10)
    val highTicker = newTicker("2017-12-08T10:10:00Z", 100)
    box.put(highTicker)

//    assert(box.isUpdatingHigh(ZonedDateTime.parse("2017-12-08T10:10:00Z"), 10) === true)
    assert(box.isUpdatingHigh(ZonedDateTime.parse("2017-12-08T10:20:00Z"), 11 * 60) === true)
  }

  def newTicker(timestamp: String, ltp: Double) =  Ticker("",timestamp,0,0,0,0,0,0,0,ltp,0,0)
}
