package domain.strategy.box

import java.time.ZonedDateTime

import domain.Side._
import domain.models.{Ticker, Ordering}
import domain.strategy.{StrategyState, Strategies, Strategy}
import domain.time.DateUtil
import repository.model.scalatrader.User


class BoxReverseLimitStrategy(st: StrategyState, user: User) extends Strategy(st, user) {

  var limit: Option[Double] = None
  var stop: Option[Double] = None
  var entryTime: Option[ZonedDateTime] = None

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {
    val now = DateUtil.now()
    val momentum = Strategies.coreData.momentum5min.values.takeRight(1).values.headOption
    val result = if (!isAvailable || Strategies.coreData.box1h.isEmpty || momentum.isEmpty) {
      None
    } else {
      val box1h = Strategies.coreData.box1h.get
      if (state.order.isEmpty) {
        val start = now.minusMinutes(16)
        val end = now.minusMinutes(15)

        if (start.isBefore(box1h.lowTime) && !end.isBefore(box1h.lowTime) && ticker.ltp < box1h.high && momentum.exists(_ > -10000) && momentum.exists(_.abs < 30000)) {
          limit = Some((box1h.high + ticker.ltp) / 2 + 1000)
          stop = Some(box1h.low)
          entry(Buy)
        } else if (start.isBefore(box1h.highTime) && !end.isBefore(box1h.highTime) && ticker.ltp > box1h.low && momentum.exists(_ < 10000) && momentum.exists(_.abs < 30000)) {
          limit = Some((box1h.low + ticker.ltp) / 2 - 1000)
          stop = Some(box1h.high)
          entry(Sell)
        } else {
          None
        }
      } else  {
        if (entryTime.map(_.plusMinutes(30)).exists(_.isBefore(DateUtil.now()))) {
          close()
        } else if (state.order.get.side == Buy) {
          if (limit.exists(_ < ticker.ltp)) {
            close()
          } else if (stop.exists(_ > ticker.ltp)) {
            close()
          } else {
            None
          }
        } else { // Sell
          if (limit.exists(_ > ticker.ltp)) {
            close()
          } else if (stop.exists(_ < ticker.ltp)) {
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