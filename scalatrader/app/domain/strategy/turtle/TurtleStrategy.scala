package domain.strategy.turtle

import domain.models.{Ticker, Ordering}
import domain.{Side, models}
import domain.Side._
import domain.strategy.{Strategy}
import repository.model.scalatrader.User


class TurtleStrategy(user: User) extends Strategy(user) {
  override def putTicker(ticker: models.Ticker): Unit = {
    core.put(ticker)
  }

  val core = new TurtleCore

  override def close(): Option[Ordering] = {
    losLimit = None
    super.close()
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
      if (entryPosition.isEmpty) {
        if (box20.high < ltp) {
          losLimit = None
          entry(Buy)
        } else if (ltp < box20.low) {
          losLimit = None
          entry(Sell)
        } else {
          None
        }
      } else if (entryPosition.get.side == Side.Sell) {
        if (losLimit.exists(_ < ltp)) {
          close
        } else if (box10.high < ltp) {
          close
        } else if (ltp < box20.low) {
          losLimit = stopRange.map(ltp + _)
          None
        } else {
          None
        }
      } else { // BUY
        if (losLimit.exists(ltp < _)) {
          close
        } else if (ltp < box10.low) {
          close
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


  override def processEvery1minutes(): Unit = {
    core.refresh()
  }

  override def init(): Unit = {
    entryPosition = None
    losLimit = None
    core.init()
  }
}