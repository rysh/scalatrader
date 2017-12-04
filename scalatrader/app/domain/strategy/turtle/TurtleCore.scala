package domain.strategy.turtle

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import com.google.gson.Gson
import domain.models
import domain.models.{Ticker}
import domain.strategy.core.Bar
import domain.time.DateUtil

import scala.collection.mutable

class TurtleCore {


  val candles1min = new mutable.HashMap[Long, Bar]()
  var bar_10min: Option[Bar] = None
  var bar_20min: Option[Bar] = None

  def init(): Unit = {
    candles1min.clear()
    bar_10min = None
    bar_20min = None
  }

  def loadInitialData(list: Seq[(Long, Iterator[String])]) = {
    init()
    val gson: Gson = new Gson
    list.foreach{case (key, lines) => {
      lines.foreach(json => {
        val ticker: Ticker = gson.fromJson(json, classOf[Ticker])
        candles1min.get(key) match {
          case Some(v) => v.put(ticker.ltp)
          case _ => candles1min.put(key, new Bar(key))
        }
      })
    }}
  }

  def put(ticker: models.Ticker) = {
    val key = DateUtil.keyOfUnit1Minutes(ZonedDateTime.parse(ticker.timestamp))
    candles1min.get(key) match {
      case Some(v) => v.put(ticker.ltp)
      case _ => {
        val b = new Bar(key)
        candles1min.put(key, b.put(ticker.ltp))
      }
    }
    bar_10min.map(_.put(ticker.ltp))
    bar_20min.map(_.put(ticker.ltp))
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
      val in20 = Some(Bar.of(values))
      val values10 = values.filter(b => key10 <= b.key)
      if (values10.size > 0) {
        bar_10min = Some(Bar.of(values10))
      }
      bar_20min = in20
    }
  }

}
