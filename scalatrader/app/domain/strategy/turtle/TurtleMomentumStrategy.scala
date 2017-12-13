package domain.strategy.turtle

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import domain.Side.{Sell, Buy}
import domain.margin.Margin
import domain.models.{Ticker, Ordering}
import domain.strategy.core.{Indices, Momentum, Box}
import domain.{Side, models}
import domain.strategy.{Strategies, Strategy}
import domain.time.DateUtil
import repository.model.scalatrader.User


class TurtleMomentumStrategy(user: User) extends Strategy {
  override def putTicker(ticker: models.Ticker): Unit = {
    core.put(ticker)
  }

  val core = new TurtleCore
  override def email: String = user.email
  override def key: String = user.api_key
  override def secret: String = user.api_secret

  var leverage = Margin.defaltLeverage
  var orderSize: Double = Margin.defaultSizeUnit * leverage

  def entry(o: Ordering): Option[Ordering] = {
    position = Some(o)
    updateSizeUnit()
    entryTime = Some(DateUtil.now())
    position
  }
  def close(): Unit = {
    position = None
    losLimit = None
    entryTime = None
  }

  var position: Option[Ordering] = None
  var entryTime: Option[ZonedDateTime] = None
  val stopRange:Option[Double] = None
  var losLimit:Option[Double] = None

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {

    val data = core.unit5min
    val momentum5min = Strategies.coreData.momentum5min
    val momentumValueOption = momentum5min.latest
    val ltp = ticker.ltp
    val now = DateUtil.now()

    val result = if (!isAvailable || data.candles.size < 22 || momentumValueOption.isEmpty) {
      None
    } else {
      val box10 = data.box10.get
      val box20 = data.box20.get
      val momentum = new TurtleMomentumValue(momentumValueOption.get, momentum5min)
      if (position.isEmpty) {
        val m5 = momentum5min.values.takeRight(1).values.headOption
//        println(m5)
        if (box20.high < ltp && momentum.isAvailableBuyEntry && m5.exists(_ > 0)) {
          losLimit = stopRange.map(ltp - _)
          entry(Ordering(Buy, orderSize))
        } else if (ltp < box20.low && momentum.isAvailableSellEntry && m5.exists(_ < 0)) {
          losLimit = stopRange.map(ltp + _)
          entry(Ordering(Sell, orderSize))
        } else {
          None
        }
      } else  {
        if (position.get.side == Sell) {
          if (losLimit.exists(_ < ltp)) {
            close()
            Some(Ordering(Buy, position.map(_.size).getOrElse(orderSize)))
          } else if (box10.high < ltp && momentum.isAvailableToBuyClose) {
            close()
            Some(Ordering(Buy, position.map(_.size).getOrElse(orderSize)))
          } else if (box20.high < ltp) {
            close()
            Some(Ordering(Buy, position.map(_.size).getOrElse(orderSize)))
//          } else if (entryTime.exists(now.minusMinutes(10).isBefore(_)) && isFake(Sell)) {
//            close()
//            Some(Ordering(Buy, position.map(_.size).getOrElse(orderSize)))
          } else if (ltp < box20.low) {
            losLimit = stopRange.map(ltp + _)
            None
          } else {
            None
          }
        } else { // BUY
          if (losLimit.exists(ltp < _)) {
            close()
            Some(Ordering(Sell, position.map(_.size).getOrElse(orderSize)))
          } else if (ltp < box10.low && momentum.isAvailableToSellClose) {
            close()
            Some(Ordering(Sell, position.map(_.size).getOrElse(orderSize)))
          } else if (ltp < box20.low) {
            close()
            Some(Ordering(Sell, position.map(_.size).getOrElse(orderSize)))
//          } else if (entryTime.exists(now.minusMinutes(10).isBefore(_)) && isFake(Buy)) {
//            close()
//            Some(Ordering(Sell, position.map(_.size).getOrElse(orderSize)))
          } else if (box20.high < ltp) {
            losLimit = stopRange.map(ltp - _)
            None
          } else {
            None
          }
        }
      }
    }
    result
  }

  def isFake(side: String): Boolean = {
    val bars = Strategies.coreData.candles10min.values.takeRight(2).map(_._2)
    if (bars.size != 2) {
      return false
    }
    val old = bars.head
    val current = bars.last
    if (side == Sell) {
      old.high < current.high && old.low < current.low
    } else {
      old.high > current.high && old.low > current.low
    }
  }

  private def updateSizeUnit(): Unit = {
    val newSize = Margin.sizeUnit * leverage
    if (orderSize < newSize) {
      println(s"orderSize($orderSize) -> newSize($newSize)")
      orderSize = newSize
    }
  }


  override def processEvery1minutes(): Unit = {
    core.refresh()
  }

  override def init(): Unit = {
    position = None
    losLimit = None
    core.init()
    leverage = Margin.leverage
    orderSize = Margin.sizeUnit * leverage
  }
}

class TurtleMomentumValue(t:(Long,Double), data: Momentum) {
  val key: Long = t._1
  val value: Double = t._2

  def isAvailableBuyEntry: Boolean = value > data.box1h.buyEntrySign
  def isAvailableSellEntry: Boolean = value < data.box1h.sellEntrySign
  def isAvailableToBuyClose: Boolean = value > data.box1h.buyCloseSign
  def isAvailableToSellClose: Boolean = value < data.box1h.sellCloseSign

  override def toString = s"TurtleMomentumValue(key=$key, value=$value, isAvailableBuyEntry=$isAvailableBuyEntry, isAvailableSellEntry=$isAvailableSellEntry, isAvailableToBuyClose=$isAvailableToBuyClose, isAvailableToSellClose=$isAvailableToSellClose)"
}