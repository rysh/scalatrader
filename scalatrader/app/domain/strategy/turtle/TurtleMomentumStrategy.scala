package domain.strategy.turtle

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import domain.Side.{Sell, Buy}
import domain.margin.Margin
import domain.models.{Ticker, Ordering}
import domain.{Side, models}
import domain.strategy.{Strategy, Strategies}
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

  var leverage = 2.0
  var orderSize: Double = Margin.defaultSizeUnit * leverage
  var position: Option[Ordering] = None
  def entry(o: Ordering): Option[Ordering] = {
    position = Some(o)
    updateSizeUnit()
    position
  }
  def close(): Unit = {
    position = None
    losLimit = None
  }

  val stopRange:Option[Double] = Some(3000)
  var losLimit:Option[Double] = None
  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {

    val data = core.unit10sec
    val momentumOption = Strategies.coreData.momentum20.latest

    val ltp = ticker.ltp
    val result = if (!isAvailable || data.candles.size < 22 || momentumOption.isEmpty) {
      None
    } else {
      val box10 = data.box10.get
      val box20 = data.box20.get
      val momentum = new TurtleMomentumValue(momentumOption.get)
      if (position.isEmpty) {
        if (box20.high < ltp && momentum.isAvailableBuyEntry) {
          losLimit = None
          entry(Ordering(Side.Buy, orderSize))
        } else if (ltp < box20.low && momentum.isAvailableSellEntry) {
          losLimit = None
          entry(Ordering(Side.Sell, orderSize))
        } else {
          None
        }
      } else  {
        if (position.get.side == Side.Sell) {
          if (losLimit.exists(_ < ltp)) {
            close()
            Some(Ordering(Side.Buy, position.map(_.size).getOrElse(orderSize)))
          } else if (momentum.isNeedToBuyClose) {
            close()
            Some(Ordering(Side.Buy, position.map(_.size).getOrElse(orderSize)))
          } else if (box10.high < ltp && momentum.isAvailableToBuyClose) {
            close()
            Some(Ordering(Side.Buy, position.map(_.size).getOrElse(orderSize)))
          } else if (ltp < box20.low) {
            losLimit = stopRange.map(ltp + _)
            None
          } else {
            None
          }
        } else { // BUY
          if (losLimit.exists(ltp < _)) {
            close()
            Some(Ordering(Side.Sell, position.map(_.size).getOrElse(orderSize)))
          } else if (momentum.isNeedToSellClose) {
            close()
            Some(Ordering(Side.Sell, position.map(_.size).getOrElse(orderSize)))
          } else if (ltp < box10.low && momentum.isAvailableToSellClose) {
            close()
            Some(Ordering(Side.Sell, position.map(_.size).getOrElse(orderSize)))
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

class TurtleMomentumValue(t:(Long,Double)) {
  val key: Long = t._1
  val value: Double = t._2

  def isAvailableBuyEntry: Boolean = value > 1000
  def isAvailableSellEntry: Boolean = value < -1000
  def isAvailableToBuyClose: Boolean = value > -500
  def isAvailableToSellClose: Boolean = value < 500
  def isNeedToBuyClose: Boolean = value > 0
  def isNeedToSellClose: Boolean = value < 0

  override def toString = s"TurtleMomentumValue(key=$key, value=$value, isAvailableBuyEntry=$isAvailableBuyEntry, isAvailableSellEntry=$isAvailableSellEntry, isAvailableToBuyClose=$isAvailableToBuyClose, isAvailableToSellClose=$isAvailableToSellClose, isNeedToBuyClose=$isNeedToBuyClose, isNeedToSellClose=$isNeedToSellClose)"
}