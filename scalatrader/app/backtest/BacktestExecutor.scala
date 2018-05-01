package backtest

import java.time.ZonedDateTime

import domain.backtest.BackTestResults.OrderResult
import domain.backtest.{BackTestResults, WaitingOrder}
import domain.models
import domain.models.{Orders, Ticker}
import domain.strategy.{Strategies, Strategy}

class BacktestExecutor(val strategy: Strategy) {
  def execute(ticker: Ticker): Unit = {

    try {
      val time = ZonedDateTime.parse(ticker.timestamp)
      if (!WaitingOrder.isWaitingOrJustExecute(strategy.email, time, order => {
            println(order)
            BackTestResults.put(strategy.state.id, OrderResult(ticker.timestamp, order.side, ticker.ltp, order.size))
          })) {
        (try {
          strategy.judgeByTicker(ticker)
        } catch {
          case e: Exception =>
            e.printStackTrace()
            None
        }).map(Orders.market)
          .foreach((order: models.Order) => {
            WaitingOrder.request(strategy.email, time, order)
          })
      }
    } catch {
      case e: Exception => throw e
    } finally {
      Strategies.putTicker(ticker)
      BackTestResults.addTicker(ticker)
    }
  }
}
