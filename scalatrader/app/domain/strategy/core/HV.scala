package domain.strategy.core

import domain.time.DateUtil

import scala.collection.mutable

class HV(candles: mutable.LinkedHashMap[Long, Bar], term: Int) {

  val values = new mutable.LinkedHashMap[Long, Double]()

  def init(): Unit = {
    values.clear
  }
  def update(key: Long): Unit = {
    if (candles.size > term) {
      values.put(key, calc(key))
    }
  }

  def clean(key: Long): Unit = {
    values.keys.filter(_ < key).foreach(values.remove)
  }

  def calc(key: Long): Double = {
    val now = DateUtil.parseKey(key)
    val start = DateUtil.keyOf(now.minusMinutes(term + 1), 60)
    val days: Seq[Double] = candles.filter(p => {
      start < p._1 && p._1 <= key
    }).values.map(_.close).toSeq
    val currentDays = days.tail
    val previousDays = days.init
    std(currentDays.zip(previousDays).map { case (cur, prv) => cur / prv })
  }

  def std(buff: Seq[Double]): Double = {
    val size: Int = buff.size
    val avg = buff.sum / size
    Math.sqrt(buff.map(x => (x - avg) * (x - avg)).sum / size) * 100
  }

}
