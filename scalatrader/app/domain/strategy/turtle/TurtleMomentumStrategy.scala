package domain.strategy.turtle

import java.time.ZonedDateTime

import domain.Side.{Sell, Buy}
import domain.models.{Ticker, Ordering}
import domain.strategy.core.Momentum
import domain.models
import domain.strategy.{Strategies, Strategy, StrategyState}
import domain.time.DateUtil
import repository.model.scalatrader.User


class TurtleMomentumStrategy(state: StrategyState, user: User) extends Strategy(state, user) {
  override def putTicker(ticker: models.Ticker): Unit = {
    core.put(ticker)
  }

  val core = new TurtleCore

  override def entry(side: String): Option[Ordering] = {
    entryTime = Some(DateUtil.now())
    super.entry(side)
  }
  override def close(): Option[Ordering] = {
    losLimit = None
    entryTime = None
    super.close()
  }

  var entryTime: Option[ZonedDateTime] = None
  val stopRange:Option[Double] = None
  var losLimit:Option[Double] = None

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {

    val data = core.unit5min
    val momentum5min = Strategies.coreData.momentum5min
    val momentumValueOption = momentum5min.latest
    val ltp = ticker.ltp

    val result = if (!isAvailable || data.candles.size < 22 || momentumValueOption.isEmpty) {
      None
    } else {
      val box10 = data.box10.get
      val box20 = data.box20.get
      val momentum = new TurtleMomentumValue(momentumValueOption.get, momentum5min)
      if (state.order.isEmpty) {
        val m5 = momentum5min.values.takeRight(1).values.headOption
        if (box20.high < ltp && momentum.isAvailableBuyEntry && m5.exists(_ > 0)) {
          losLimit = stopRange.map(ltp - _)
          entry(Buy)
        } else if (ltp < box20.low && momentum.isAvailableSellEntry && m5.exists(_ < 0)) {
          losLimit = stopRange.map(ltp + _)
          entry(Sell)
        } else {
          None
        }
      } else  {
        if (state.order.get.side == Sell) {
          if (losLimit.exists(_ < ltp)) {
            close()
          } else if (box10.high < ltp && momentum.isAvailableToBuyClose) {
            close()
          } else if (box20.high < ltp) {
            close()
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
          } else if (ltp < box10.low && momentum.isAvailableToSellClose) {
            close()
          } else if (ltp < box20.low) {
            close()
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
    val bars = Strategies.coreData.candles10min.values.takeRight(2).values
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


  override def processEvery1minutes(): Unit = {
    core.refresh()
  }

  override def init(): Unit = {
    super.init()
    losLimit = None
    core.init()
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