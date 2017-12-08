package domain.strategy.core

import scala.collection.mutable

class Momentum(candles: mutable.LinkedHashMap[Long, Bar], candleDuration: Int, momentumDuration: Int) {
  val values = new mutable.LinkedHashMap[Long, Double]()

  def update(key: Long): Unit = {
    candles.get(key).foreach(put)
  }
  def clean(key: Long): Unit = {
    values.keys.filter(_ < key).foreach(values.remove)
  }

  def put(bar: Bar): Unit = calc(candles, candleDuration, momentumDuration, bar.key).foreach(t => values.put(t._1, t._2))

  def loadAll(): Unit = candles.keys.toSeq.sorted.foreach(key => candles.get(key).foreach(put))

  def calc(candles: mutable.LinkedHashMap[Long, Bar], candleDuration: Int, momentumDuration: Int, key: Long): Option[(Long, Double)] = {
    val ret = for {
      p3 <- candles.get(key - momentumDuration * candleDuration * 2)
      p2 <- candles.get(key - momentumDuration * candleDuration * 1)
      p1 <- candles.get(key - momentumDuration * candleDuration)
      d3 <- candles.get(key - candleDuration * 2)
      d2 <- candles.get(key - candleDuration * 1)
      d1 <- candles.get(key)
      m1 = d1.close - p1.close
      m2 = d2.close - p2.close
      m3 = d3.close - p3.close
    } yield {
      (d3.key, (((m1 + m2 + m3) / 3) * 100).toLong / 100.0)
    }
    ret
  }

  def latest: Option[(Long,Double)] = values.lastOption

  def clear(): Unit = {
    values.clear()
  }
}
