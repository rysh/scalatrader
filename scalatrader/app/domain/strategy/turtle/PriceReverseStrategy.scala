package domain.strategy.turtle

import domain.margin.Margin
import domain.models.{Ticker, Ordering}
import domain.{Side, models}
import domain.strategy.Strategy
import repository.model.scalatrader.User


class PriceReverseStrategy(user: User) extends Strategy {
  override def putTicker(ticker: models.Ticker) = {
    core.put(ticker)
  }

  val core = new TurtleCore
  override def email = user.email
  override def key = user.api_key
  override def secret = user.api_secret

  var leverage = Margin.defaltLeverage
  var orderSize: Double = Margin.defaultSizeUnit * leverage
  var position: Option[Ordering] = None
  def entry(o: Ordering): Option[Ordering] = {
    position = Some(o)
    updateSizeUnit
    stopLine = None
    limitLine = None
    position
  }
  def close = {
    position = None
    stopLine = None
    limitLine = None
  }

  val limitRange:Option[Double] = Some(3000)
  var limitLine:Option[Double] = None
  val stopRange:Option[Double] = Some(500)
  var stopLine:Option[Double] = None
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
          entry(Ordering(Side.Sell, orderSize))
        } else if (ltp < box20.low) {
          entry(Ordering(Side.Buy, orderSize))
        } else {
          None
        }
      } else if (position.get.side == Side.Buy) {
        if (stopLine.exists(_ < ltp) | limitLine.exists(ltp < _)) {
          close
          Some(Ordering(Side.Sell, position.map(_.size).getOrElse(orderSize)))
        } else if (box10.high < ltp) {
          close
          Some(Ordering(Side.Sell, position.map(_.size).getOrElse(orderSize)))
        } else if (ltp < box20.low) {
          stopLine = stopRange.map(ltp + _)
          limitLine = limitRange.map(ltp - _)
          None
        } else {
          None
        }
      } else { // BUY
        if (stopLine.exists(ltp < _) || limitLine.exists(_ < ltp)) {
          close
          Some(Ordering(Side.Buy, position.map(_.size).getOrElse(orderSize)))
        } else if (ltp < box10.low) {
          close
          Some(Ordering(Side.Buy, position.map(_.size).getOrElse(orderSize)))
        } else if (box20.high < ltp) {
          stopLine = stopRange.map(ltp - _)
          limitLine = limitRange.map(ltp + _)
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
      println(s"orderSize($orderSize) -> newSize($newSize)")
      orderSize = newSize
    }
  }


  override def processEvery1minutes(): Unit = {
    core.refresh()
  }

  override def init(): Unit = {
    position = None
    stopLine = None
    core.init()
    leverage = Margin.defaltLeverage
    orderSize = Margin.defaultSizeUnit * leverage
  }
}