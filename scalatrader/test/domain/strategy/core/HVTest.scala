package domain.strategy.core

import org.scalatest.FunSuite

import scala.collection.mutable

class HVTest extends FunSuite {

  test("testStd") {
    import DummyCandle._
    val candles = new mutable.LinkedHashMap[Long, Bar]()
    candles.put(1L, new Bar(1).put(newTicker("1", 10.0)))
    candles.put(2L, new Bar(2).put(newTicker("2", 20.0)))
    candles.put(3L, new Bar(3).put(newTicker("3", 30.0)))
    candles.put(4L, new Bar(4).put(newTicker("3", 40.0)))
    val hv = new HV(candles, 3)
    hv.update(4)

  }

  test("testUpdate") {

  }

}
