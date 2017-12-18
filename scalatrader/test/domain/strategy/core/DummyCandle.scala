package domain.strategy.core

import domain.models.Ticker
import domain.time.DateUtil

import scala.collection.mutable

object DummyCandle {

  def createCandle(): mutable.LinkedHashMap[Long, Bar] = {
    val map = new mutable.LinkedHashMap[Long, Bar]()
    (1 to 100).map(i => {
      val time = toTimestamp(i)
      new Bar(DateUtil.keyOf(time, 60)).put(newTicker(time.toOffsetDateTime.toString, (1000 + (i * ((50 - i) / 50.0))).toLong))
    })
      .foreach(v => map.put(v.key, v))
    map
  }

  private def toTimestamp(i: Int) = DateUtil.parseKey(20171201000000L).plusMinutes(i)


  def newTicker(timestamp: String, ltp: Double) =  Ticker("",timestamp,0,0,0,0,0,0,0,ltp,0,0)
}
