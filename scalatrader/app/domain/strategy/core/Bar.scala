package domain.strategy.core

import domain.models

class Bar(arg: Long) {
  var key: Long = arg
  var high: Double = Double.MinValue
  var low: Double = Double.MaxValue
  var open: Double = Double.MinValue
  var close: Double = Double.MinValue

  def put(ticker: models.Ticker): Bar = {
    val ltp = ticker.ltp
    put(ltp)
  }

  def put(ltp: Double): Bar = {
    if (high < ltp) high = ltp
    if (ltp < low) low = ltp
    if (open == Double.MinValue) open = ltp
    close = ltp
    this
  }

  override def toString = s"Bar($key, high:$high, low:$low, open:$open, close:$close)"
}
