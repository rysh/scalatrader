package domain.strategy.turtle

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern

import domain.models.Ticker
import play.api.Logger

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


object BackTestResults {

  def init() = {
    total = 0.0
    entry = None
    candles1min.clear()
    values.clear()
    tickers.clear()
  }


  val candles1min = new mutable.HashMap[Long, Bar]()
  val values = new mutable.ArrayBuffer[(OrderResult, OrderResult, Int, Int)]
  val tickers = new mutable.ArrayBuffer[Ticker]

  var total: Double = 0.0
  var entry: Option[OrderResult] = None
  def add(order: OrderResult): Unit = {

    if (entry.isEmpty) {
      entry = Some(order)
    } else {

      val value = calc(entry.get, order)
      total = total + value
      values += ((entry.get, order, value.toInt, total.toInt))
      println(format(entry.get, order, value, total))
      entry = None
    }
  }

  case class OrderResult(timestamp:String, side:String, price:Double, size: Double)


  def report() = {
    var total: Double = 0.0
    values.foreach{ case (entry, close, _, _) => {
      val value = calc(entry, close)
      total = total + value
      println(format(entry, close, value, total))
    }}
    println(s"最終損益 $total")
  }

  private def calc(entry: OrderResult, close: OrderResult) = {
    (close.price - entry.price) * entry.size * (if (close.side == "SELL") 1 else -1)
  }

  def format(entry: OrderResult, close: OrderResult, value: Double, total:Double) = {
    def parse(timestamp: String) = {
      ZonedDateTime.parse(timestamp).format(DateTimeFormatter.ofPattern("MM/dd HH:mm:ss"))
    }
    s"${parse(entry.timestamp)} ${entry.side} -> ${parse(close.timestamp)} ${close.side} / ${entry.price} -> ${close.price} / 損益 $value : 累積損益 $total"
  }

  def valuesForChart(): ArrayBuffer[(String, Int, OrderResult, Int)] = {

    values.flatMap{case (entry, close, value, cumulative) => List(("entry", cumulative - value, entry, 0),("close", cumulative, close, value))}
  }

  def addTicker(ticker: Ticker) = {
    if (domain.isBackTesting) {
      if (tickers.size == 0 || tickers.last.ltp != ticker.ltp) tickers += ticker
    }
  }
}
