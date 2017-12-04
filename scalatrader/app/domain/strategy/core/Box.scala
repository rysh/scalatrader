package domain.strategy.core

import domain.models

class Box(key: Long, duration: Long) {
  var high: Double = Double.MinValue
  var low: Double = Double.MaxValue
  var open: Double = Double.MinValue
  var close: Double = Double.MinValue

  def put(bar: Bar): Box = {
    if (high < bar.high) high = bar.high
    if (bar.low < low) low = bar.low
    if (bar.key < key) open = bar.open
    if (key < bar.key) close = bar.close
    this
  }

  def put(ticker: models.Ticker): Box = {
    val ltp = ticker.ltp
    if (high < ltp) high = ltp
    if (ltp < low) low = ltp
    if (open == Double.MinValue) open = ltp
    close = ltp
    this
  }

  def put(bars: Seq[Bar]): Box ={
    bars.foreach(put)
    this
  }
}

object Box {
  def of(bars: Seq[Bar], duration: Long): Box = {
    new Box(bars.map(_.key).min, duration).put(bars)
  }
}
