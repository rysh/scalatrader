package domain.strategy.turtle

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern

import play.api.Logger

import scala.collection.mutable


object BackTestResults {
  def init() = {
    total = 0.0
    entry = None
    values.clear()
  }


  val candles1min = new mutable.HashMap[Long, Bar]()
  val values = new mutable.ArrayBuffer[(OrderResult, OrderResult, Int)]

  var total: Double = 0.0
  var entry: Option[OrderResult] = None
  def add(order: OrderResult): Unit = {

    if (entry.isEmpty) {
      entry = Some(order)
    } else {

      val value = calc(entry.get, order)
      total = total + value
      values += ((entry.get, order, total.toInt))
      println(format(entry.get, order, value, total))
      entry = None
    }
  }

  case class OrderResult(timestamp:String, side:String, price:Double, size: Double)


  def report() = {
    var total: Double = 0.0
    values.foreach{ case (entry, close, _) => {
      val value = calc(entry, close)
      total = total + value
      println(format(entry, close, value, total))
    }}
    println(s"最終損益 $total")
  }

  private def calc(entry: OrderResult, close: OrderResult) = {
    (close.price - entry.price) * (if (close.side == "SELL") 1 else -1)
  }

  def format(entry: OrderResult, close: OrderResult, value: Double, total:Double) = {
    def parse(timestamp: String) = {
      ZonedDateTime.parse(timestamp).format(DateTimeFormatter.ofPattern("MM/dd HH:mm:ss"))
    }
    s"${parse(entry.timestamp)} ${entry.side} -> ${parse(close.timestamp)} ${close.side} / ${entry.price} -> ${close.price} / 損益 $value : 累積損益 $total"
  }

}
