package domain.strategy.sfd

import java.time.ZonedDateTime

import domain.Side._
import domain.models.{Ticker, Ordering}
import domain.strategy.{StrategyState, Strategies, Strategy}
import domain.time.DateUtil
import repository.model.scalatrader.User

class SfdStrategy(st: StrategyState, user: User) extends Strategy(st, user) {

  var entryTime: Option[ZonedDateTime] = None

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {
    import Strategies._
    DateUtil.now()

    val result = if (coreData.btcCurrent.isEmpty) {
      None
    } else {
      val sfd = new SFD(coreData.btcCurrent.get, ticker)

      None
      if (state.order.isEmpty) {
        if (sfd.buyZone) {
          entry(Buy)
        } else if (sfd.sellZone) {
          entry(Sell)
        } else {
          None
        }
      } else {
        if (state.order.get.side == Buy) {
          if (sfd.sellZone) {
            if (sfd.tooClose) {
              None
            } else {
              close()
            }
          } else if (sfd.buyZone) {
            None
          } else {
            close()
          }
        } else { // Sell
          if (sfd.buyZone) {
            if (sfd.tooClose) {
              None
            } else {
              close()
            }
          } else if (sfd.sellZone) {
            None
          } else {
            close()
          }
        }
      }
    }
    if (result.isDefined) {
      val t = coreData.btcCurrent.get
      println(s"SfdStrategy      ${ticker.timestamp} ${new SFD(t, ticker).deviationRate.toString().substring(0, 6)}% ${t.ltp} ${ticker.ltp}")
    }
    result
  }
  override def entry(size: String): Option[Ordering] = {
    entryTime = Some(DateUtil.now())
    super.entry(size)
  }
}
