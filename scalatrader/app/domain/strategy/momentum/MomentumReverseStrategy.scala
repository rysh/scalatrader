package domain.strategy.momentum

import domain.Side.{Sell, Buy}
import domain.models.{Ticker, Ordering}
import domain.strategy.{Strategies, Strategy, StrategyState}
import repository.model.scalatrader.User


class MomentumReverseStrategy(st: StrategyState, user: User) extends Strategy(st, user) {

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {

    val momentum5min = Strategies.coreData.momentum5min
    val momentum = momentum5min.values.values.takeRight(3).toSeq

    val result = if (!isAvailable || momentum.lengthCompare(3) < 0) {
      None
    } else {
      val one = momentum.head
      val two = momentum.tail.head
      val three = momentum.last
      if (state.order.isEmpty) {
        if (one < two && two > three) {
          entry(Buy)

        } else if (one > two && two < three) {
          entry(Sell)

        } else {
          None
        }
      } else  {
        if (state.order.get.side == Sell) {
          if (one < two && two > three) {
            close()

          } else {
            None
          }
        } else { // BUY
          if (one > two && two < three) {
            close()

          } else {
            None
          }
        }
      }
    }
    result
  }
}