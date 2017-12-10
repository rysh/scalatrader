package domain.strategy.core

import scala.collection.mutable

class Momentum(candles: mutable.LinkedHashMap[Long, Bar], candleDuration: Int, momentumDuration: Int = 10) {
  val values = new mutable.LinkedHashMap[Long, Double]()
  var box1h: MomentumBox = new MomentumBox(Seq.empty)

  def update(key: Long): Unit = {
    candles.get(key).foreach(put)
    box1h = new MomentumBox(values.values.toSeq)
  }
  def clean(key: Long): Unit = {
    values.keys.filter(_ < key).foreach(values.remove)
  }

  def put(bar: Bar): Unit = calc(candles, candleDuration, momentumDuration, bar.key).foreach(t => values.put(t._1, t._2))

  def loadAll(): Unit = {
    candles.keys.toSeq.sorted.foreach(key => candles.get(key).foreach(put))
    box1h = new MomentumBox(values.values.toSeq)
  }

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
class MomentumBox(momentum: Seq[Double]) {
  var high: Double = Double.MinValue
  var low: Double = Double.MaxValue

  momentum.foreach(put)

  def put(newValue: Double): Unit = {
    if (high < newValue) high = newValue
    if (newValue < low) low = newValue
  }
  def absValue: Double = {
    val abs = if (high < low.abs) low.abs else high
    if (abs > 10000) abs else 10000.0
  }

  def buyEntrySign: Double = absValue * 0.4
  def sellEntrySign: Double = - absValue * 0.4
  def buyCloseSign: Double = absValue * 0.2
  def sellCloseSign: Double = - absValue * 0.2
//  def buyEntrySign: Double = 5000
//  def sellEntrySign: Double = -5000
//  def buyCloseSign: Double = 4000
//  def sellCloseSign: Double = -4000
}