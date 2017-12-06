package domain.strategy.turtle

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import com.google.gson.Gson
import domain.models
import domain.models.Ticker
import domain.strategy.core.{Bar, Box}
import domain.time.DateUtil

import scala.collection.mutable

class TurtleCore {


  val candles1min = new mutable.HashMap[Long, Bar]()
  var bar_10min: Option[Box] = None
  var bar_20min: Option[Box] = None

  val candles10sec = new mutable.HashMap[Long, Bar]()
  var box10sec: Option[Box] = None
  var box20sec: Option[Box] = None

  def init(): Unit = {
    candles1min.clear()
    bar_10min = None
    bar_20min = None
    candles10sec.clear()
    box10sec = None
    box20sec = None
  }

  def put(ticker: models.Ticker) = {
    putTicker(ticker)
  }

  private def putTicker(ticker: Ticker) = {
    val now = ZonedDateTime.parse(ticker.timestamp)
    val key = DateUtil.keyOfUnit1Minutes(now)
    candles1min.get(key) match {
      case Some(v) => v.put(ticker)
      case _ => {
        val b = new Bar(key)
        candles1min.put(key, b.put(ticker))
      }
    }
    bar_10min.map(_.put(ticker))
    bar_20min.map(_.put(ticker))

    val key10Sec = DateUtil.keyOfUnitSeconds(now, 10)
    candles10sec.get(key10Sec) match {
      case Some(v) => v.put(ticker)
      case _ => {
        candles10sec.put(key10Sec, new Bar(key10Sec).put(ticker))
        val oldKey = DateUtil.keyOfUnitSeconds(now.minus(21 * 10, ChronoUnit.SECONDS), 10)
        candles10sec.keys.filter(key => key < oldKey).foreach(key => candles10sec.remove(key))
        val values: Seq[Bar] = candles10sec.values.toSeq.sortBy(_.key)
        val in20 = Some(Box.of(values, 20))
        val in10 = Some(Box.of(values.takeRight(11), 10))
        box10sec = in10
        box20sec = in20
      }
    }
    box10sec.map(_.put(ticker))
    box20sec.map(_.put(ticker))
  }

  def refresh() = {
    val now = DateUtil.now()

    def keyOfBefore(min:Int) =
      DateUtil.keyOfUnit1Minutes(now.minus(min, ChronoUnit.MINUTES))

    val key20 = keyOfBefore(20)
    val key10 = keyOfBefore(10)
    
    candles1min.keys.filter(key => key < key20).foreach(key => candles1min.remove(key))

    val values = candles1min.values.toSeq.sortBy(_.key)
    if (values.size > 0) {
      val in20 = Some(Box.of(values, 20))
      val values10: Seq[Bar] = values.filter(b => key10 <= b.key)
      if (values10.size > 0) {
        bar_10min = Some(Box.of(values10, 10))
      }
      bar_20min = in20
    }
  }

}
