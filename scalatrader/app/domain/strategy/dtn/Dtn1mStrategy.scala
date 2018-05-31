package domain.strategy.dtn

import java.time.ZonedDateTime

import domain.Side._
import domain.models.{Ticker, Ordering}
import domain.strategy.box.TriBox
import domain.strategy.core.Bar
import domain.strategy.{StrategyState, Strategies, Strategy}
import domain.time.DateUtil
import repository.model.scalatrader.User

class Dtn1mStrategy(st: StrategyState, user: User) extends Strategy(st, user) {

  var entryTime: Option[ZonedDateTime] = None
  var dtn: Dtn = _
  var lossCut: DtnLossCut = _

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {

    val boxes = new TriBox(Strategies.coreData.box10min, Strategies.coreData.box20min, Strategies.coreData.box1h)
    val result =
      if (!isAvailable || Strategies.coreData.box1h.isEmpty || !boxes.isDefined) {
        None
      } else {

        val recentBars = Strategies.coreData.candles1min.values.takeRight(7).values

        dtn = new Dtn(recentBars)
        if (state.order.isEmpty) {
          if (boxes.isUp && dtn.breakLong) {
            entry(Sell)
          } else if (boxes.isDown && dtn.breakShort) {
            entry(Buy)
          } else {
            None
          }
        } else {
          if (state.order.get.side != Buy) {
            if (dtn.breakShort || lossCut.leaveRangeOnUnderSide(ticker)) {
              close()
            } else {
              if (lossCut.leaveRangeOnUpperSide(ticker)) {
                lossCut = dtn.toLossCut
              }
              None
            }
          } else { // ! Sell
            if (dtn.breakLong || lossCut.leaveRangeOnUpperSide(ticker)) {
              close()
            } else {
              if (lossCut.leaveRangeOnUnderSide(ticker)) {
                lossCut = dtn.toLossCut
              }
              None
            }
          }
        }
      }
    result
  }

  override def entry(size: String): Option[Ordering] = {
    entryTime = Some(DateUtil.now())
    lossCut = dtn.toLossCut
    super.entry(size)
  }
}
