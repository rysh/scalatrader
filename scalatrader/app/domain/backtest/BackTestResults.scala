package domain.backtest

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import domain.models.Ticker
import domain.strategy.core.Bar
import domain.time.DateUtil

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


object BackTestResults {

  def init(): Unit = {
    total = 0
    entry = None
    candles1min.clear()
    values.clear()
    tickers.clear()
    depositMargin = 300000
  }

  var depositMargin: Long = 300000

  val candles1min = new mutable.HashMap[Long, Bar]()
  val values = new mutable.ArrayBuffer[(OrderResult, OrderResult, Int, Int)]
  val tickers = new mutable.ArrayBuffer[Ticker]
  val momentum = new mutable.LinkedHashMap[Long, Double]()

  var total: Int = 0
  var entry: Option[OrderResult] = None
  def add(order: OrderResult): Unit = {

    if (entry.isEmpty) {
      entry = Some(order)
    } else {

      val value = calc(entry.get, order).toInt
      depositMargin = depositMargin + value
      total = total + value
      values += ((entry.get, order, value, total))
      println(format(entry.get, order, value, total))
      entry = None
    }
  }

  case class OrderResult(timestamp:String, side:String, price:Double, size: Double)


  def report() = {
    var total: Int = 0
    values.foreach{ case (entry, close, _, _) => {
      val value = calc(entry, close).toInt
      total = total + value
      println(format(entry, close, value, total))
    }}
    println(s"最終損益 $total")
  }

  private def calc(entry: OrderResult, close: OrderResult) = {
    (close.price - entry.price) * entry.size * (if (close.side == "SELL") 1 else -1)
  }

  def format(entry: OrderResult, close: OrderResult, value: Int, total:Int) = {
    def parse(timestamp: String) = {
      ZonedDateTime.parse(timestamp).format(DateTimeFormatter.ofPattern("MM/dd HH:mm:ss"))
    }
    s"${parse(entry.timestamp)} ${entry.side} -> ${parse(close.timestamp)} ${close.side} / ${entry.price} -> ${close.price} (${entry.size})/ 損益 $value : 累積損益 $total"
  }

  def valuesForChart(): ArrayBuffer[(String, Int, OrderResult, Int)] = {

    values.flatMap{case (entry, close, value, cumulative) => List(("entry", cumulative - value, entry, 0),("close", cumulative, close, value))}
  }

  def addTicker(ticker: Ticker) = {
    if (tickers.size == 0 || tickers.last.ltp != ticker.ltp) tickers += ticker

    val key = DateUtil.keyOfUnit1Minutes(ZonedDateTime.parse(ticker.timestamp))
    candles1min.get(key) match {
      case Some(v) => v.put(ticker)
      case _ => {
        val b = new Bar(key)
        candles1min.put(key, b.put(ticker))
      }
    }
  }
}
