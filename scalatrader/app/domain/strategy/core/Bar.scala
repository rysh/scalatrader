package domain.strategy.core

import java.time.ZonedDateTime

import domain.time.DateUtil

class Bar(arg: Long) {
  var key: Long = arg
  var high: Double = Double.MinValue
  var low: Double = Double.MaxValue
  var open: Double = Double.MinValue
  var close: Double = Double.MinValue

  def put(ltp: Double): Bar = {
    if (high < ltp) high = ltp
    if (ltp < low) low = ltp
    if (open == Double.MinValue) open = ltp
    close = ltp
    this
  }

  override def toString = s"Bar($key, high:$high, low:$low, opwn:$open, close:$close)"
}

object Bar {
  def of(bars: Seq[Bar]):Bar = {
    if (bars.size == 0) {
      Bar.now()
    } else {
      var bar = new Bar(bars.head.key)
      bars.foreach(b => {
        bar.put(b.open)
        bar.put(b.low)
        bar.put(b.high)
        bar.put(b.close)
      })
      bar
    }
  }

  //Bar.ofに呼ばれずに新しい値がputされる前提の場合のみ使用可
  def now(): Bar = {
    new Bar(DateUtil.keyOfUnit1Minutes(ZonedDateTime.now()))
  }
}