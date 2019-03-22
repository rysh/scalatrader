package domain.strategy.box

import java.time.ZonedDateTime

import domain.Side._
import domain.models.{Ticker, Ordering}
import domain.strategy.{StrategyState, Strategies, Strategy}
import domain.time.DateUtil
import repository.model.scalatrader.User

class UpTrendStrategy(st: StrategyState, user: User) extends Strategy(st, user) {

  var entryTime: Option[ZonedDateTime] = None

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {
    val now = DateUtil.now()
    val momentum = Strategies.coreData.momentum5min.values.takeRight(1).values.headOption

    

    val result = if (!isAvailable || Strategies.coreData.box1h.isEmpty || momentum.isEmpty) {
      None
    } else {
      val box10min = Strategies.coreData.box10min.get
      val box20min = Strategies.coreData.box20min.get
      val box1h = Strategies.coreData.box1h.get
      lazy val isUpdatingHigh = box1h.isUpdatingHigh(now, 60 * 20)
      if (state.order.isEmpty) {
        if (box10min.isUp && box20min.isUp && box1h.isUp && isUpdatingHigh) {
          entry(Buy)
        } else {
          None
        }
      } else {
        if (!isUpdatingHigh || box20min.low + 500 > ticker.ltp) {
          close()
        } else {
          None
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
