package domain.strategy.box

import java.time.ZonedDateTime

import domain.Side._
import domain.models.{Ordering, Ticker}
import domain.strategy.{Strategies, Strategy, StrategyState}
import domain.time.DateUtil
import repository.model.scalatrader.User

class DownTrend2hStrategy(st: StrategyState, user: User) extends Strategy(st, user) {

  var entryTime: Option[ZonedDateTime] = None

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {
    val now = DateUtil.now()

    val result = if (!isAvailable || Strategies.coreData.box4h.isEmpty) {
      None
    } else {
      val shortRange = Strategies.coreData.box20min.get
      val middleRange = Strategies.coreData.box1h.get
      val longRange = Strategies.coreData.box2h.get
      lazy val isUpdatingLow = longRange.isUpdatingLow(now, 120 * 20)
      if (state.order.isEmpty) {
        if (shortRange.isDown && middleRange.isDown && longRange.isDown && isUpdatingLow) {
          entry(Sell)
        } else {
          None
        }
      } else {
        if (!isUpdatingLow || middleRange.high < ticker.ltp) {
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
