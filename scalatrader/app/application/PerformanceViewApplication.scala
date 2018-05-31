package application

import java.math.MathContext
import java.time.{ZonedDateTime, ZoneId}

import adapter.BitFlyer
import adapter.BitFlyer.MyExecution
import com.google.inject.{Inject, Singleton}
import domain.Side
import domain.strategy.StrategyState
import domain.time.DateUtil
import play.api.{Configuration, Logger}
import repository.{RecordRepository, StrategyRepository, UserRepository}
import repository.model.scalatrader.{User, TradingRecord2}

@Singleton
class PerformanceViewApplication @Inject()(config: Configuration) {

  case class PerformanceSummary(total: Long, average: Long, maxDD: Long, count: Long)

  def amountOfMoney(executions: Seq[MyExecution]): BigDecimal = {
    executions
      .map(e => {
        val price: BigDecimal = e.price
        val size: BigDecimal = e.size
        toInteger(price * size * (if (Side.Buy == e.side) 1 else -1))
      })
      .sum
  }

  def amountOfPrice(executions: Seq[MyExecution]): BigDecimal = {
    val money = amountOfMoney(executions)
    toInteger(money / executions.map(_.size).sum)
  }

  private def toInteger(a: BigDecimal) = a.setScale(0, scala.math.BigDecimal.RoundingMode.HALF_UP)

  def marginGain(entry: Seq[MyExecution], close: Seq[MyExecution]): BigDecimal = {
    if (entry.nonEmpty && close.nonEmpty) {
      (amountOfMoney(close) + amountOfMoney(entry)) * -1
    } else 0
  }

  def marginPriceGain(entry: Seq[MyExecution], close: Seq[MyExecution]): BigDecimal = {
    if (entry.nonEmpty && close.nonEmpty) {
      (amountOfPrice(close) + amountOfPrice(entry)) * -1
    } else 0
  }

  private case class Result(user: User, strategy: StrategyState, results: List[BigDecimal])

  def summary(email: String, strategyId: Long, from: ZonedDateTime): Option[PerformanceSummary] = {
    val result: Seq[BigDecimal] = RecordRepository.findAll(email, strategyId, from).map(r => marginPriceGain(r.entryExecution, r.closeExecution))
    if (result.nonEmpty) {
      val sum = result.sum
      Some(PerformanceSummary(sum.toLong, (sum / result.size).setScale(0, scala.math.BigDecimal.RoundingMode.HALF_UP).toLong, result.min.toLong, result.size))
    } else {
      None
    }
  }
}
