package domain.backtest

import domain.backtest.BackTestResults._
import play.api.Logger

import scala.collection.mutable

class BackTestResult(val strategyId: Long) {

  var total: Int = 0
  var temporalEntry: Option[OrderResult] = None
  val values: mutable.ArrayBuffer[(OrderResult, OrderResult, Int, Int)] = mutable.ArrayBuffer()

  def put(order: OrderResult): Unit = {

    temporalEntry
      .map(entry => {
        val difference = calcAbsoluteDifferenceOfPrice(entry, order).toInt
        depositMargin = depositMargin + difference
        total = total + difference
        Logger.info(format(strategyId, entry, order, difference, total))

        temporalEntry = None
        valueMaps.get(strategyId).map(_ += ((entry, order, difference, total)))
      })
      .getOrElse(() => {
        temporalEntry = Some(order)
      })
  }

  private def calcAbsoluteDifferenceOfPrice(entry: OrderResult, close: OrderResult) = {
    (close.price - entry.price) * entry.size * (if (close.side == "SELL") 1 else -1)
  }
}
