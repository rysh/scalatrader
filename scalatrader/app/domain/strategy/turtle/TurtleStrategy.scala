package domain.strategy.turtle

import domain.margin.Margin
import domain.models.{Ticker, Ordering}
import domain.{Side, models}
import domain.strategy.{Strategies, Strategy}
import repository.model.scalatrader.User


class TurtleStrategy(user: User) extends Strategy {
  override def putTicker(ticker: models.Ticker) = {
    core.put(ticker)
  }

  val core = new TurtleCore
  override def email = user.email
  override def key = user.api_key
  override def secret = user.api_secret

  var leverage = 2.0
  var orderSize = 0.2 * leverage
  var position: Option[Ordering] = None
  def entry(o: Ordering): Option[Ordering] = {
    println("entry")
    position = Some(o)
    updateSizeUnit
    position
  }
  def close = {
    println("close")
    position = None
    losLimit = None
  }

  var losLimit:Option[Double] = None
  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {
    val ltp = ticker.ltp

    val result = if (!isAvailable || core.candles10sec.size < 22) {
      None
    } else {
      val box10 = core.box10sec.get
      val box20 = core.box20sec.get
      if (position.isEmpty) {
        if (box20.high < ltp) {
          println("box20.high < ltp")
          losLimit = None
          entry(Ordering(Side.Buy, orderSize))
        } else if (ltp < box20.low) {
          println(s"ltp($ltp) < box20.low(${box20.low})")
          losLimit = None
          entry(Ordering(Side.Sell, orderSize))
        } else {
          None
        }
      } else if (position.get.side == Side.Sell) {
        if (losLimit.exists(_ < ltp)) {
          println(s"limit(${losLimit.get}) < ltp($ltp)")
          close
          Some(Ordering(Side.Buy, position.map(_.size).getOrElse(orderSize)))
        } else if (box10.high < ltp) {
          println(s"box10.high(${box10.high}) < ltp($ltp)")
          close
          Some(Ordering(Side.Buy, position.map(_.size).getOrElse(orderSize)))
        } else if (ltp < box20.low) {
          //println(s"ltp($ltp) < box20.low(${box20.low}) not order because already have position")
          losLimit = Some(ltp + 3000)
          None
        } else {
          None
        }
      } else { // BUY
        if (losLimit.exists(ltp < _)) {
          println(s"ltp($ltp) < limit(${losLimit.get})")
          close
          Some(Ordering(Side.Sell, position.map(_.size).getOrElse(orderSize)))
        } else if (ltp < box10.low) {
          println(s"ltp($ltp) < box10.low(${box10.low})")
          close
          Some(Ordering(Side.Sell, position.map(_.size).getOrElse(orderSize)))
        } else if (box20.high < ltp) {
          //println(s"box20.high(${box20.high}) < ltp($ltp) not order because already have position")
          losLimit = Some(ltp - 3000)
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
    losLimit = None
    core.init()
    leverage = 2.0
    orderSize = 0.2 * leverage
  }
}