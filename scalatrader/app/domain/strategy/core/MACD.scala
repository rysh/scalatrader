package domain.strategy.core

import scala.collection.mutable

class MACD(longRange: Int, shortRange: Int, candleBar: mutable.LinkedHashMap[Long, Bar]) {

  def take(n: Int): Iterable[Double] = candleBar.values.takeRight(n).map(_.close)
  def last: Double = candleBar.values.last.close

  var previousKey = 0L
  var emaShort: Option[Double] = None
  var emaLong: Option[Double] = None

  def alpha(n: Int) = 2.0 / (n + 1)

  def update(): Unit = {
    val key = candleBar.keys.lastOption
    if (key.exists(_ != previousKey)) {
      previousKey = key.get
      emaShort = newEma(emaShort, shortRange)
      emaLong = newEma(emaLong, longRange)
    }
  }

  private def newEma(oldEma: Option[Double], n: Int): Option[Double] = {
    val a = alpha(n)
    oldEma.map(old => old * (1 - a) + last * a).orElse(Some(initialEma(n)))
  }

  private def initialEma(n: Int): Double = (take(n).sum + last) / (n + 1)

  def buySignal: Boolean = {
    val res = for {
      short <- emaShort
      long <- emaLong
    } yield {
      short - long > 0
    }
    res.getOrElse(false)
  }
  def sellSignal: Boolean = {
    val res = for {
      short <- emaShort
      long <- emaLong
    } yield {
      short - long < 0
    }
    res.getOrElse(false)
  }

  def clear(): Unit = {
    emaShort = None
    emaLong = None
  }
  def isEmpty(): Boolean = emaShort.isEmpty || emaLong.isEmpty

}
