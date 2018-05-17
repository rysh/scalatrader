package backtest

import java.time.ZonedDateTime

import domain.backtest.BackTestResults.OrderResult
import domain.backtest.{BackTestResult, BackTestResults, WaitingOrder}
import domain.models
import domain.models.{Orders, Ticker}
import domain.strategy.{Strategies, Strategy}

class BackTestExecutor(val strategy: Strategy) {
  val result = new BackTestResult(strategy.id)
  def execute(ticker: Ticker): Unit = {
    val time = ZonedDateTime.parse(ticker.timestamp)
    if (!WaitingOrder.isWaitingOrJustExecute(
          strategy.email,
          time,
          order => {
            println(order)
            val orderResult = OrderResult(ticker.timestamp, order.side, ticker.ltp, order.size)
            result.put(orderResult)
          }
        )) {
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
  }
}
