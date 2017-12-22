package domain.strategy.momentum

import java.time.ZonedDateTime

import domain.Side.{Sell, Buy}
import domain.models.{Ticker, Ordering}
import domain.strategy.{Strategies, Strategy, StrategyState}
import domain.time.DateUtil
import repository.model.scalatrader.User


class MomentumStrategy(state: StrategyState, user: User) extends Strategy(state, user) {

  override def entry(side: String): Option[Ordering] = {
    entryTime = Some(DateUtil.now())
    super.entry(side)
  }
  override def close(): Option[Ordering] = {
    losLimit = None
    entryTime = None
    super.close()
  }

  var entryTime: Option[ZonedDateTime] = None
  val stopRange:Option[Double] = None
  var losLimit:Option[Double] = None

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {

    val momentum5min = Strategies.coreData.momentum5min
    val latestOption = momentum5min.latest
    val previousOption = momentum5min.oneFromLast
    val ltp = ticker.ltp
    val macd = Strategies.coreData.macd5m

    val result = if (!isAvailable || latestOption.isEmpty || previousOption.isEmpty || macd.isEmpty()) {
      None
    } else {
      val latest = latestOption.get._2
      val previous = previousOption.get._2
      if (entryPosition.isEmpty) {
        if (previous < 0 && latest > 0 && ((previous - latest).abs > 2000) && macd.buySignal) {
          losLimit = stopRange.map(ltp - _)
          entry(Buy)
        } else if (previous > 0 && latest < 0 && ((previous - latest).abs > 2000) && macd.sellSignal) {
          losLimit = stopRange.map(ltp + _)
          entry(Sell)
        } else {
          None
        }
      } else  {
        if (entryPosition.get.side == Sell) {
          if (losLimit.exists(_ < ltp)) {
            close()

          } else if (latest > 0) {
            close()

          } else if (false) {
            losLimit = stopRange.map(ltp + _)
            None
          } else {
            None
          }
        } else { // BUY
          if (losLimit.exists(ltp < _)) {
            close()

          } else if (latest < 0) {
            close()

          } else if (false) {
            losLimit = stopRange.map(ltp - _)
            None
          } else {
            None
          }
        }
      }
    }
    result
  }


  override def processEvery1minutes(): Unit = {

  }

  override def init(): Unit = {
    super.init()
    losLimit = None
  }
}