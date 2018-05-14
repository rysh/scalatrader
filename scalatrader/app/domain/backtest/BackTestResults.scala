package domain.backtest

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import domain.models.Ticker
import domain.strategy.core.Bar
import domain.time.DateUtil
import play.api.Logger

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object BackTestResults {

  def init(): Unit = {
    total = 0
    tempEntries.clear()
    candles1min.clear()
    valueMaps.clear()
    tickers.clear()
    depositMargin = 300000
  }

  var depositMargin: Long = 300000

  val candles1min = new mutable.HashMap[Long, Bar]()
  val valueMaps = mutable.HashMap.empty[Long, mutable.ArrayBuffer[(OrderResult, OrderResult, Int, Int)]]
  val tickers = new mutable.ArrayBuffer[Ticker]
  val momentum = new mutable.LinkedHashMap[Long, Double]()
  val hv = new mutable.LinkedHashMap[Long, Double]()

  var total: Int = 0
  var tempEntries = mutable.Map.empty[Long, OrderResult]

  def put(strategyId: Long, order: OrderResult): Unit = {
    tempEntries
      .get(strategyId)
      .map(entry => {
        val difference = calcAbsoluteDifferenceOfPrice(entry, order).toInt
        depositMargin = depositMargin + difference
        total = total + difference
        valueMaps.get(strategyId).map(_ += ((entry, order, difference, total)))
        Logger.info(format(strategyId, entry, order, difference, total))
        tempEntries.remove(strategyId)
      })
      .getOrElse(() => {
        tempEntries.put(strategyId, order)
      })
  }

  case class OrderResult(timestamp: String, side: String, price: Double, size: Double)

  def report() = {
    var total: Int = 0
    valueMaps.foreach {
      case (id, values) =>
        values.foreach {
          case (entry, close, _, _) =>
            val value = calcAbsoluteDifferenceOfPrice(entry, close).toInt
            total = total + value
            Logger.info(format(id, entry, close, value, total))
        }
        Logger.info(s"最終損益($id) $total")
    }
  }

  private def calcAbsoluteDifferenceOfPrice(entry: OrderResult, close: OrderResult) = {
    (close.price - entry.price) * entry.size * (if (close.side == "SELL") 1 else -1)
  }

  def format(strategyId: Long, entry: OrderResult, close: OrderResult, value: Int, total: Int): String = {
    def parse(timestamp: String) = {
      ZonedDateTime.parse(timestamp).format(DateTimeFormatter.ofPattern("MM/dd HH:mm:ss"))
    }
    s"[$strategyId] ${parse(entry.timestamp)} ${entry.side} -> ${parse(close.timestamp)} ${close.side} / ${entry.price} -> ${close.price} (${entry.size})/ 損益 $value : 累積損益 $total"
  }

  def valuesForChart(): Iterable[(String, Int, OrderResult, Int)] = {
    valueMaps.values.flatten.flatMap { case (entry, close, value, cumulative) => List(("entry", cumulative - value, entry, 0), ("close", cumulative, close, value)) }
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
