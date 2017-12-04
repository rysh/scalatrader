package domain.strategy.core

import java.time.ZonedDateTime

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

  val candles1min = new mutable.HashMap[Long, Bar]()
  val positionByUser = new mutable.HashMap[String, Position]()

  def init() = {
    positionByUser.clear()
    candles1min.clear()
  }

  def putTicker(ticker: models.Ticker) = {
    val key = DateUtil.keyOfUnit1Minutes(ZonedDateTime.parse(ticker.timestamp))
    candles1min.get(key) match {
      case Some(v) => v.put(ticker.ltp)
      case _ => {
        val b = new Bar(key)
        candles1min.put(key, b.put(ticker.ltp))
      }
    }
  }


  def processEvery1minutes() = {}
}
