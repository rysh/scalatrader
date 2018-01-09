package domain.strategy.core

import java.time.ZonedDateTime

import domain.models
import domain.time.DateUtil

class Box(key: Long, duration: Long) {
  var high: Double = Double.MinValue
  var highTime: ZonedDateTime = DateUtil.now()
  var low: Double = Double.MaxValue
  var lowTime: ZonedDateTime = DateUtil.now()
  var open: Double = Double.MinValue
  var close: Double = Double.MinValue

  def put(bar: Bar): Box = {
    if (high < bar.high) {
      high = bar.high
      highTime = DateUtil.parseKey(bar.key)
    }
    if (bar.low < low) {
      low = bar.low
      lowTime = DateUtil.parseKey(bar.key)
    }
    if (bar.key <= key) open = bar.open
    if (key <= bar.key) close = bar.close
    this
  }

  def put(ticker: models.Ticker): Box = {
    val ltp = ticker.ltp
    if (high < ltp) {
      high = ltp
      highTime = ZonedDateTime.parse(ticker.timestamp)
    }
    if (ltp < low) {
      low = ltp
      lowTime = ZonedDateTime.parse(ticker.timestamp)
    }
    if (open == Double.MinValue) open = ltp
    close = ltp
    this
  }
  def put(box: Box): Box = {
    this.high = box.high
    this.highTime = box.highTime
    this.low = box.low
    this.lowTime = box.lowTime
    this.open = box.open
    this.close = box.close
    this
  }

  def put(bars: Seq[Bar]): Box ={
    bars.foreach(put)
    this
  }

  def copy(): Box = {
    new Box(this.key,this.duration).put(this)
  }

  def isUpdatingHigh(now: ZonedDateTime, time: Int): Boolean = highTime.plusSeconds(time).isAfter(now)

  def isUpdatingLow(now: ZonedDateTime, time: Int): Boolean = lowTime.plusSeconds(time).isAfter(now)

  def isUp: Boolean = highTime.isAfter(lowTime) && open < close
  def isDown: Boolean = lowTime.isAfter(highTime) && open > close

  override def toString = s"Box(high=$high, low=$low, open=$open, close=$close, highTime=$highTime, lowTime=$lowTime)"
}

object Box {
  def of(bars: Seq[Bar], duration: Long): Box = {
    new Box(bars.map(_.key).min, duration).put(bars)
  }
}
