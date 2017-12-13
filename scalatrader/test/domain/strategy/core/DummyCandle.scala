package domain.strategy.core

import domain.models.Ticker

import scala.collection.mutable

object DummyCandle {

  def createCandle(): mutable.LinkedHashMap[Long, Bar] = {
    val map = new mutable.LinkedHashMap[Long, Bar]()
    (1 to 100).map(i => new Bar(i).put(newTicker(i.toString, (1000 + (i * ((50 - i) / 50.0))).toLong)))
      .foreach(v => map.put(v.key, v))
    map
  }

  def newTicker(timestamp: String, ltp: Double) =  Ticker("",timestamp,0,0,0,0,0,0,0,ltp,0,0)
}
