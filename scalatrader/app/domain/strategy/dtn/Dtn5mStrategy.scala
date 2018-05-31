package domain.strategy.dtn

import java.time.ZonedDateTime

import domain.Side._
import domain.models.{Ticker, Ordering}
import domain.strategy.core.{Box, CandleBar, Bar}
import domain.strategy.{StrategyState, Strategies, Strategy}
import domain.time.DateUtil
import repository.model.scalatrader.User

class Dtn5mStrategy(st: StrategyState, user: User) extends Strategy(st, user) {

  var entryTime: Option[ZonedDateTime] = None

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {

    val result = if (!isAvailable || Strategies.coreData.box1h.isEmpty) {
      None
    } else {

      val recentBars = Strategies.coreData.candles5min.values.takeRight(7).values

      val dtn = new Dtn(recentBars)

      if (state.order.isEmpty) {
        if (dtn.breakLong) {
          entry(Buy)
        } else if (dtn.breakShort) {
          entry(Sell)
        } else {
          None
        }
      } else {
        if (state.order.get.side == Buy) {
          if (dtn.breakShort) {
            close()
          } else {
            None
          }
        } else { // Sell
          if (dtn.breakLong) {
            close()
          } else {
            None
          }
        }
      }
    }
    result
  }

  override def entry(size: String): Option[Ordering] = {
    entryTime = Some(DateUtil.now())
    super.entry(size)
  }
}
