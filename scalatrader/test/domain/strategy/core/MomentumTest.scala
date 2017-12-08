package domain.strategy.core

import domain.models.Ticker
import org.scalatest.FunSuite

import scala.collection.mutable

class MomentumTest extends FunSuite {

  test("testOf") {
    val candles = createCandle
    val momentum = new Momentum(candles, 1, 10)
    momentum.loadAll()
    //momentum.values.foreach(println)
  }

  def createCandle(): mutable.LinkedHashMap[Long, Bar] = {
    val map = new mutable.LinkedHashMap[Long, Bar]()
    (1 to 100).map(i => new Bar(i).put(newTicker(i.toString, (1000 + (i * ((50 - i) / 50.0))).toLong)))
      .foreach(v => map.put(v.key, v))
    map
  }

  def newTicker(timestamp: String, ltp: Double) =  Ticker("",timestamp,0,0,0,0,0,0,0,ltp,0,0)
}
