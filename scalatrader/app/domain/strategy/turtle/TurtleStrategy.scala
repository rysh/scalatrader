package domain.strategy.turtle

import domain.margin.Margin
import domain.models.{Ticker, Ordering}
import domain.{Side, models}
import domain.strategy.{Strategies, Strategy}
import play.api.Logger
import repository.model.scalatrader.User


class TurtleStrategy(user: User) extends Strategy {
  override def putTicker(ticker: models.Ticker): Unit = {
    core.put(ticker)
  }

  val core = new TurtleCore
  override def email = user.email
  override def key = user.api_key
  override def secret = user.api_secret

  var leverage = Margin.defaultLeverage
  var orderSize: Double = Margin.defaultSizeUnit * leverage
  var position: Option[Ordering] = None
  def entry(o: Ordering): Option[Ordering] = {
    position = Some(o)
    updateSizeUnit
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

    val ltp = ticker.ltp
    val result = if (!isAvailable || data.candles.size < 22) {
      None
    } else {
      val box10 = data.box10.get
      val box20 = data.box20.get
      if (position.isEmpty) {
        if (box20.high < ltp) {
          losLimit = None
          entry(Ordering(Side.Buy, orderSize, true))
        } else if (ltp < box20.low) {
          losLimit = None
          entry(Ordering(Side.Sell, orderSize, true))
        } else {
          None
        }
      } else if (position.get.side == Side.Sell) {
        if (losLimit.exists(_ < ltp)) {
          close
          Some(Ordering(Side.Buy, position.map(_.size).getOrElse(orderSize), false))
        } else if (box10.high < ltp) {
          close
          Some(Ordering(Side.Buy, position.map(_.size).getOrElse(orderSize), false))
        } else if (ltp < box20.low) {
          losLimit = stopRange.map(ltp + _)
          None
        } else {
          None
        }
      } else { // BUY
        if (losLimit.exists(ltp < _)) {
          close
          Some(Ordering(Side.Sell, position.map(_.size).getOrElse(orderSize), false))
        } else if (ltp < box10.low) {
          close
          Some(Ordering(Side.Sell, position.map(_.size).getOrElse(orderSize), false))
        } else if (box20.high < ltp) {
          losLimit = stopRange.map(ltp - _)
          None
        } else {
          None
        }
      }
    }
    result
  }

  private def updateSizeUnit = {
    val newSize = Margin.sizeUnit * leverage
    if (orderSize < newSize) {
      Logger.info(s"orderSize($orderSize) -> newSize($newSize)")
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
    leverage = Margin.defaultLeverage
    orderSize = Margin.defaultSizeUnit * leverage
  }
}