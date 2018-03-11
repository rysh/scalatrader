package domain.strategy.box

import java.time.ZonedDateTime

import domain.Side._
import domain.models.{Ticker, Ordering}
import domain.strategy.{StrategyState, Strategies, Strategy}
import domain.time.DateUtil
import repository.model.scalatrader.User

class MixedBoxesStrategy(st: StrategyState, user: User) extends Strategy(st, user) {

  var limit: Option[Double] = None
  var stop: Option[Double] = None
  var entryTime: Option[ZonedDateTime] = None

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {
    val now = DateUtil.now()
    val momentum = Strategies.coreData.momentum5min.values.takeRight(1).values.headOption

    val result = if (!isAvailable || Strategies.coreData.box1h.isEmpty || momentum.isEmpty) {
      None
    } else {
      val box20min = Strategies.coreData.box20min.get
      val box1h = Strategies.coreData.box1h.get
      if (state.order.isEmpty) {
        val start = now.minusMinutes(6)
        val end = now.minusMinutes(5)

        def isAsc(start: ZonedDateTime, time: ZonedDateTime, end: ZonedDateTime) = {
          start.isBefore(time) && !end.isBefore(time)
        }

        lazy val tooLargeVolatility = box1h.high - box1h.low > 30000
        lazy val anObviousDownwardTrend = momentum.exists(_ < -10000)
        lazy val anObviousUpwardTrend = momentum.exists(_ > 10000)

        if (isAsc(start, box20min.lowTime, end)
            && !anObviousDownwardTrend && !tooLargeVolatility) {
          limit = Some((box1h.high + ticker.ltp) / 2 + 1000)
          stop = Some(box20min.low)
          entry(Buy)
        } else if (isAsc(start, box20min.highTime, end)
                   && !anObviousUpwardTrend && !tooLargeVolatility) {
          limit = Some((box1h.low + ticker.ltp) / 2 - 1000)
          stop = Some(box20min.high)
          entry(Sell)
        } else {
          None
        }
      } else {
        if (entryTime.map(_.plusMinutes(10)).exists(_.isBefore(DateUtil.now()))) {
          close()
        } else if (state.order.get.side == Buy) {
          def isUpdatingHigh = box1h.isUpdatingHigh(now, 60 * 20)
          if (limit.exists(_ < ticker.ltp) && !isUpdatingHigh) {
            close()
          } else if (stop.exists(_ > ticker.ltp)) {
            close()
          } else {
            None
          }
        } else { // Sell
          def isUpdatingLow = box1h.isUpdatingLow(now, 60 * 20)
          if (limit.exists(_ > ticker.ltp) && !isUpdatingLow) {
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
