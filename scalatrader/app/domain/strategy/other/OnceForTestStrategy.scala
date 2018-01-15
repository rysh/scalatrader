package domain.strategy.other

import domain.Side._
import domain.models.{Ticker, Ordering}
import domain.strategy.{StrategyState, Strategy}
import repository.model.scalatrader.User


class OnceForTestStrategy(st: StrategyState, user: User) extends Strategy(st, user) {

  var done = false

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {

    if (state.order.isEmpty) {
      if (!done) {
        done = true
        entry(Buy)
      } else {
        None
      }
    } else  {
      close()
    }
  }
}