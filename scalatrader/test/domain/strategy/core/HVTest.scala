package domain.strategy.core

import org.scalatest.FunSuite

import scala.collection.mutable

class HVTest extends FunSuite {

  test("testStd") {
    import DummyCandle._
    val candles = new mutable.LinkedHashMap[Long, Bar]()
    candles.put(20171201000100L, new Bar(20171201000100L).put(newTicker("2017-12-01T00:01.000 Z", 10.0)))
    candles.put(20171201000200L, new Bar(20171201000200L).put(newTicker("2017-12-01T00:02.000 Z", 20.0)))
    candles.put(20171201000300L, new Bar(20171201000300L).put(newTicker("2017-12-01T00:03.000 Z", 30.0)))
    candles.put(20171201000400L, new Bar(20171201000400L).put(newTicker("2017-12-01T00:04.000 Z", 40.0)))
    val hv = new HV(candles, 3)
    hv.update(20171201000400L)
  }
}
