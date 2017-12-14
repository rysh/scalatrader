package domain.strategy.core

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import domain.models

import scala.collection.mutable
import domain.time.DateUtil

class CoreData {
  val dataKeepTime = 1

  val candles10sec = new CandleBar(10)
  val candles20sec = new CandleBar(20)
  val candles30sec = new CandleBar(30)
  val candles1min = new CandleBar(60)
  val candles5min = new CandleBar(300)
  val candles10min = new CandleBar(600)

  var box10min: Option[Box] = None
  var box20min: Option[Box] = None
  var box1h: Option[Box] = None

  val momentum10 = new Momentum(candles10sec.values, 10)
  val momentum20 = new Momentum(candles20sec.values, 20)
  val momentum1min = new Momentum(candles1min.values, 60)
  val momentum5min = new Momentum(candles5min.values, 5 * 60)

  val macd1m = new MACD(26,12, candles1min.values)
  val macd5m = new MACD(26,12, candles5min.values)

  def init() = {
    candles10sec.clear()
    candles20sec.clear()
    candles30sec.clear()
    candles1min.clear()
    candles5min.clear()
    candles10min.clear()
    momentum10.clear()
    momentum20.clear()
    momentum1min.clear()
    momentum5min.clear()
    macd1m.clear()
    macd5m.clear()
  }

  def putTicker(ticker: models.Ticker) = {
    val now = ZonedDateTime.parse(ticker.timestamp)

    candles1min.put(now, ticker, _ => {
      momentum1min.update(DateUtil.keyOf(now.minusMinutes(1), 60))
      momentum1min.clean(DateUtil.keyOf(now.minusHours(dataKeepTime * 2)))
    })
    candles10min.put(now, ticker, _ => {})

    candles5min.put(now, ticker, _ => {
      momentum5min.update(DateUtil.keyOf(now.minusMinutes(5), 300))
      momentum5min.clean(DateUtil.keyOf(now.minusHours(dataKeepTime * 2), 300))
    })

    candles10sec.put(now, ticker, _ => {
      momentum10.update(DateUtil.keyOf(now.minusSeconds(10), 10))
      momentum10.clean(DateUtil.keyOf(now.minusHours(dataKeepTime), 10))
    })

    candles20sec.put(now, ticker, _ => {
      momentum20.update(DateUtil.keyOf(now.minusSeconds(20), 20))
      momentum20.clean(DateUtil.keyOf(now.minusHours(dataKeepTime), 20))
    })

    candles30sec.put(now, ticker, _ => {})

    box10min.foreach(_.put(ticker))
    box20min.foreach(_.put(ticker))
    box1h.foreach(_.put(ticker))
  }


  def processEvery1minutes(): Unit = {
    val now = DateUtil.now()

    candles10sec.cleanCandle(now, 60)
    candles20sec.cleanCandle(now, 60)
    candles30sec.cleanCandle(now, 60)
    candles1min.cleanCandle(now, 120)
    candles10min.cleanCandle(now, 120)
    macd1m.update()
    macd5m.update()

//    val c4h = candles1min.values
//    val c2h = c4h.takeRight(120)
    val c1h = candles1min.values.values
    if (c1h.isEmpty) {
      ()
    } else {
      val c20m = c1h.takeRight(20)
      val c10m = c20m.takeRight(10)
      //    box4h = Some(Box.of(c4h.toSeq, 240 * 60))
      //    box2h = Some(Box.of(c2h.toSeq, 120 * 60))
      box1h = Some(Box.of(c1h.toSeq, 60 * 60))
      box20min = Some(Box.of(c20m.toSeq, 20 * 60))
      box10min = Some(Box.of(c10m.toSeq, 10 * 60))
    }
  }
}
