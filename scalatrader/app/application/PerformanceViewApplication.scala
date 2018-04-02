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
  val secret = config.get[String]("play.http.secret.key")

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
      (amountOfMoney(close) + amountOfMoney(entry)) * -1
    } else 0
  }

  private case class Result(user: User, strategy: StrategyState, results: List[BigDecimal])

  def hoge(): Unit = {
    //TODO 期間指定
    val datetime = ZonedDateTime.of(2018, 2, 25, 17, 0, 0, 0, DateUtil.zoneTokyo)

    val results: Seq[Result] = for {
      user <- UserRepository.everyoneWithApiKey(secret)
      strategy <- StrategyRepository.list(user)
    } yield {
      Result(user, strategy, RecordRepository.findAll(user.email, strategy.id, datetime).map(r => marginPriceGain(r.entryExecution, r.closeExecution)))
    }

    for (r <- results) {
      println(s"${r.user.email}, ${r.strategy.id}")
      val sum = r.results.sum
      if (r.results.nonEmpty) {
        println(sum)
        println(sum / r.results.size)
        println(r.results.min)
      } else {
        println("It's empty!")
      }
    }
  }
}