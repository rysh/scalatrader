package domain.strategy.core

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import domain.backtest.BackTestResults
import domain.models

import scala.collection.mutable
import domain.models.Position
import domain.time.DateUtil
import repository.model.scalatrader.User

class CoreData {
  def updatePosition(user: User, position: Option[Position]) = {
    position.map(p => positionByUser.put(user.email, p))
    if (position.isEmpty) {
      positionByUser.remove(user.email)
    }
  }

  val candles10sec = new mutable.LinkedHashMap[Long, Bar]()
  val candles20sec = new mutable.LinkedHashMap[Long, Bar]()
  val candles30sec = new mutable.LinkedHashMap[Long, Bar]()
  val candles1min = new mutable.LinkedHashMap[Long, Bar]()
  val positionByUser = new mutable.HashMap[String, Position]()

  def init() = {
    positionByUser.clear()
    candles1min.clear()
  }

  def putTicker(ticker: models.Ticker) = {
    val key = DateUtil.keyOfUnit1Minutes(ZonedDateTime.parse(ticker.timestamp))
    candles1min.get(key) match {
      case Some(v) => v.put(ticker)
      case _ => candles1min.put(key, new Bar(key).put(ticker))
    }

    val key10Sec = DateUtil.keyOfUnitSeconds(ZonedDateTime.parse(ticker.timestamp), 10)
    candles10sec.get(key10Sec) match {
      case Some(v) => v.put(ticker)
      case _ => candles10sec.put(key10Sec, new Bar(key10Sec).put(ticker))
    }

    val key20Sec = DateUtil.keyOfUnitSeconds(ZonedDateTime.parse(ticker.timestamp), 20)
    candles20sec.get(key20Sec) match {
      case Some(v) => v.put(ticker)
      case _ => candles20sec.put(key20Sec, new Bar(key20Sec).put(ticker))
    }

    val key30Sec = DateUtil.keyOfUnitSeconds(ZonedDateTime.parse(ticker.timestamp), 30)
    candles30sec.get(key30Sec) match {
      case Some(v) => v.put(ticker)
      case _ => candles30sec.put(key30Sec, new Bar(key30Sec).put(ticker))
    }
  }


  def processEvery1minutes() = {
    val now = DateUtil.now()

    def keyOfBefore(min:Int) =
      DateUtil.keyOfUnitSeconds(now.minus(min, ChronoUnit.MINUTES), 10)
    
    val key60 = keyOfBefore(60)
    candles1min.keys.filter(key => key < key60).foreach(key => candles10sec.remove(key))
    candles1min.keys.filter(key => key < key60).foreach(key => candles20sec.remove(key))
    candles1min.keys.filter(key => key < key60).foreach(key => candles30sec.remove(key))
  }
}
