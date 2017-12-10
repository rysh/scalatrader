package domain.strategy.core

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import domain.models.Ticker
import domain.time.DateUtil

import scala.collection.mutable

class CandleBar(duration: Int) {

  val values = new mutable.LinkedHashMap[Long, Bar]()

  def clear() = {
    values.clear()
  }

  def put(now:ZonedDateTime, ticker:Ticker, func: () => Unit) = {

    val key10Sec = DateUtil.keyOf(now, duration)
    values.get(key10Sec) match {
      case Some(v) => v.put(ticker)
      case _ => {
        values.put(key10Sec, new Bar(key10Sec).put(ticker))
        func()
      }
    }
  }

  def cleanCandle(now:ZonedDateTime, before: Int): Unit = {
    val refKey = DateUtil.keyOf(now.minus(before, ChronoUnit.MINUTES), 10)
    values.keys.filter(key => key < refKey).foreach(key => values.remove(key))
  }
}
