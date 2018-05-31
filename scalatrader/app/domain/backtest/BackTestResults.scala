package domain.backtest

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import backtest.BackTestExecutor
import domain.models.Ticker
import domain.strategy.core.Bar
import domain.time.DateUtil
import play.api.Logger

import scala.collection.mutable

object BackTestResults {

  def init(): Unit = {
    candles1min.clear()
    tickers.clear()
    depositMargin = 300000

    map.clear()
  }

  var depositMargin: Long = 300000

  val candles1min = new mutable.HashMap[Long, Bar]()
  val tickers = new mutable.ArrayBuffer[Ticker]
  val momentum = new mutable.LinkedHashMap[Long, Double]()
  val hv = new mutable.LinkedHashMap[Long, Double]()
//  val valueMaps = mutable.HashMap.empty[Long, mutable.ArrayBuffer[(OrderResult, OrderResult, Int, Int)]]

  var total: Int = 0
  val map = new mutable.HashMap[Long, BackTestResult]()

  case class OrderResult(timestamp: String, side: String, price: Double, size: Double)

  def printSummary(executors: Seq[BackTestExecutor]): Unit = {
    executors.foreach(e => {
      BackTestResults.report(e.result)
    })
  }

  def report(result1: BackTestResult): Unit = {
    var total: Int = 0
    result1.values.foreach {
      case (entry, close, _, _) =>
        val value = calcAbsoluteDifferenceOfPrice(entry, close).toInt
        total = total + value
        Logger.info(format(result1.strategyId, entry, close, value, total))
        Logger.info(s"最終損益(${result1.strategyId}) $total")
    }
  }

  private def calcAbsoluteDifferenceOfPrice(entry: OrderResult, close: OrderResult) = {
    (close.price - entry.price) * entry.size * (if (close.side == "SELL") 1 else -1)
  }

  def format(strategyId: Long, entry: OrderResult, close: OrderResult, value: Int, total: Int): String = {
    def parse(timestamp: String): String = {
      ZonedDateTime.parse(timestamp).format(DateTimeFormatter.ofPattern("MM/dd HH:mm:ss"))
    }
    s"[$strategyId] ${parse(entry.timestamp)} ${entry.side} -> ${parse(close.timestamp)} ${close.side} / ${entry.price} -> ${close.price} (${entry.size})/ 損益 $value : 累積損益 $total"
  }

  def valuesForChart(executors: Seq[BackTestExecutor]): Iterable[(String, Int, OrderResult, Int)] = {
    executors.flatMap(e => {
      e.result.values.flatMap { case (entry, close, value, cumulative) => List(("entry", cumulative - value, entry, 0), ("close", cumulative, close, value)) }
    })
  }

  def addTicker(ticker: Ticker): Object = {
    if (tickers.isEmpty || tickers.last.ltp != ticker.ltp) tickers += ticker

    val key = DateUtil.keyOf(ZonedDateTime.parse(ticker.timestamp))
    candles1min.get(key) match {
      case Some(v) => v.put(ticker)
      case _       => candles1min.put(key, new Bar(key).put(ticker))
    }
  }
}
