package domain.strategy.turtle

import domain.Side._
import domain.models.{Ticker, Ordering}
import domain.{Side, models}
import domain.strategy.Strategy
import repository.model.scalatrader.User


class PriceReverseStrategy(user: User) extends Strategy(user) {

  val core = new TurtleCore

  override def entry(side: String): Option[Ordering] = {
    stopLine = None
    limitLine = None
    super.entry(side)
  }
  override def close(): Option[Ordering] = {
    stopLine = None
    limitLine = None
    super.close()
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
      if (entryPosition.isEmpty) {
        if (box20.high < ltp) {
          entry(Sell)

        } else if (ltp < box20.low) {
          entry(Buy)

        } else {
          None
        }
      } else if (entryPosition.get.side == Side.Buy) {
        if (stopLine.exists(_ < ltp) | limitLine.exists(ltp < _)) {
          close
        } else if (box10.high < ltp) {
          close
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
        } else if (ltp < box10.low) {
          close()
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

  override def putTicker(ticker: models.Ticker): Unit = {
    core.put(ticker)
  }

  override def processEvery1minutes(): Unit = {
    core.refresh()
  }

  override def init(): Unit = {
    super.init()
    stopLine = None
    core.init()
  }
}