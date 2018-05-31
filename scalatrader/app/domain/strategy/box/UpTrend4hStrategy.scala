package domain.strategy.box

import java.time.ZonedDateTime

import domain.Side._
import domain.models.{Ordering, Ticker}
import domain.strategy.core.Box
import domain.strategy.{Strategies, Strategy, StrategyState}
import domain.time.DateUtil
import repository.model.scalatrader.User

class UpTrend4hStrategy(st: StrategyState, user: User) extends Strategy(st, user) {

  var entryTime: Option[ZonedDateTime] = None

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {
    val now = DateUtil.now()

    val boxes = new TriBox(Strategies.coreData.box1h, Strategies.coreData.box2h, Strategies.coreData.box4h)

    val result = if (!isAvailable || !boxes.isDefined) {
      None
    } else {
      val shortRange = Strategies.coreData.box1h.get
      val middleRange = Strategies.coreData.box2h.get
      val longRange = Strategies.coreData.box4h.get
      lazy val isUpdatingHigh = longRange.isUpdatingHigh(now, 240 * 20)
      if (state.order.isEmpty) {
        if (boxes.isUp && isUpdatingHigh) {
          entry(Buy)
        } else {
          None
        }
      } else {
        if (!isUpdatingHigh || middleRange.low > ticker.ltp) {
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
