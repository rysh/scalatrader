package domain.strategy.turtle

import java.time.ZonedDateTime

import domain.models
import domain.models.Position
import domain.time.DateUtil

import scala.collection.mutable

object TurtleCore {


  val candles1min = new mutable.HashMap[Long, Bar]()
  var bar_10min: Option[Bar] = None
  var bar_20min: Option[Bar] = None
  val positionByUser = new mutable.HashMap[String, Position]()


  def put(ticker: models.Ticker) = {
    val key = DateUtil.keyOfUnit1Minutes(ZonedDateTime.parse(ticker.timestamp))
    candles1min.get(key) match {
      case Some(v) => v.put(ticker.ltp)
      case _ => candles1min.put(key, new Bar(key).put(ticker.ltp))
    }
    bar_10min.map(_.put(ticker.ltp))
    bar_20min.map(_.put(ticker.ltp))
  }
}
