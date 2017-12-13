package domain.strategy.core

import org.scalatest.FunSuite

import scala.collection.mutable

class MACDTest extends FunSuite {

  test("") {
    import DummyCandle._
    val candles = new mutable.LinkedHashMap[Long, Bar]()
    candles.put(1L, new Bar(1).put(newTicker("1", 100.0)))
    candles.put(2L, new Bar(2).put(newTicker("2", 100.0)))
    candles.put(3L, new Bar(3).put(newTicker("3", 50.0)))
    val macd = new MACD(3, 2, candles)
    macd.update()
    assert(macd.emaShort.get.toInt === 66)
    assert(macd.emaLong.get.toInt === 75)
    candles.put(4L, new Bar(4).put(newTicker("4", 20.0)))
    macd.update()
    assert(macd.emaShort.get.toInt === 35)
    assert(macd.emaLong.get.toInt === 47)

    println("hoge")
  }
}
