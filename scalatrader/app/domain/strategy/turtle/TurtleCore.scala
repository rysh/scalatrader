package domain.strategy.turtle

import java.time.ZonedDateTime
import java.time.temporal.{ChronoUnit, TemporalUnit}

import com.google.gson.Gson
import domain.models
import domain.models.Ticker
import domain.strategy.core.{Bar, Box}
import domain.time.DateUtil

import scala.collection.mutable

class TurtleCore {


  var unit1min:TurtleData = new1min
  var unit10sec:TurtleData = new10sec
  var unit30sec:TurtleData = new30sec

  def init(): Unit = {
    unit1min = new1min
    unit10sec = new10sec
    unit30sec = new30sec
  }

  private def new1min =
    TurtleData(new mutable.HashMap[Long, Bar](), None, None, 1, ChronoUnit.MINUTES, now => DateUtil.keyOfUnit1Minutes(now))

  private def new10sec =
    TurtleData(new mutable.HashMap[Long, Bar](), None, None, 10, ChronoUnit.SECONDS, now => DateUtil.keyOfUnitSeconds(now, 10))

  private def new30sec =
    TurtleData(new mutable.HashMap[Long, Bar](), None, None, 30, ChronoUnit.SECONDS, now => DateUtil.keyOfUnitSeconds(now, 30))


  def put(ticker: models.Ticker): Option[Box] = {
    putTicker(ticker)
  }

  private def putTicker(ticker: Ticker) = {
    val now = ZonedDateTime.parse(ticker.timestamp)
    unit1min.put(ticker, now)
    unit10sec.put(ticker, now)
    unit30sec.put(ticker, now)
  }

  def refresh(): Unit = {
  }
}
case class TurtleData(candles:mutable.HashMap[Long, Bar], var box10: Option[Box], var box20: Option[Box],term: Long, unit: TemporalUnit, keyGen:ZonedDateTime => Long) {
  def put(ticker: Ticker, now: ZonedDateTime) = {
    val key = keyGen(now)
    candles.get(keyGen(now)) match {
      case Some(v) => v.put(ticker)
      case _ => {
        candles.put(key, new Bar(key).put(ticker))
        val oldKey = keyGen(now.minus(21 * term, unit))
        candles.keys.filter(key => key < oldKey).foreach(key => candles.remove(key))
        val values: Seq[Bar] = candles.values.toSeq.sortBy(_.key)
        val in20 = Some(Box.of(values, 20))
        val in10 = Some(Box.of(values.takeRight(11), 10))
        box10 = in10
        box20 = in20
      }
    }
    box10.map(_.put(ticker))
    box20.map(_.put(ticker))
  }
}
