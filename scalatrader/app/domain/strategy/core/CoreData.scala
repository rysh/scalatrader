package domain.strategy.core

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import domain.models

import scala.collection.mutable
import domain.time.DateUtil

class CoreData {
  val dataKeepTime = 1

  val candles10sec = new mutable.LinkedHashMap[Long, Bar]()
  val candles20sec = new mutable.LinkedHashMap[Long, Bar]()
  val candles30sec = new mutable.LinkedHashMap[Long, Bar]()
  val candles1min = new mutable.LinkedHashMap[Long, Bar]()

  var box10min: Option[Box] = None
  var box20min: Option[Box] = None
  var box1h: Option[Box] = None

  val momentum10 = new Momentum(candles10sec, 10)
  val momentum20 = new Momentum(candles20sec, 20)
  val momentum1min = new Momentum(candles1min, 60)

  def init() = {
    candles10sec.clear()
    candles20sec.clear()
    candles30sec.clear()
    candles1min.clear()
    momentum10.clear()
    momentum20.clear()
    momentum1min.clear()
  }

  def putTicker(ticker: models.Ticker) = {
    val now = ZonedDateTime.parse(ticker.timestamp)
    val key = DateUtil.keyOfUnit1Minutes(now)
    candles1min.get(key) match {
      case Some(v) => v.put(ticker)
      case _ => {
        candles1min.put(key, new Bar(key).put(ticker))
        momentum1min.update(DateUtil.keyOfUnit1Minutes(now.minusMinutes(1)))
        momentum1min.clean(DateUtil.keyOfUnit1Minutes(now.minusHours(dataKeepTime)))
      }
    }

    val key10Sec = DateUtil.keyOfUnitSeconds(now, 10)
    candles10sec.get(key10Sec) match {
      case Some(v) => v.put(ticker)
      case _ => {
        candles10sec.put(key10Sec, new Bar(key10Sec).put(ticker))
        momentum10.update(DateUtil.keyOfUnitSeconds(now.minusSeconds(10), 10))
        momentum10.clean(DateUtil.keyOfUnitSeconds(now.minusHours(dataKeepTime), 10))
      }
    }

    val key20Sec = DateUtil.keyOfUnitSeconds(now, 20)
    candles20sec.get(key20Sec) match {
      case Some(v) => v.put(ticker)
      case _ => {
        candles20sec.put(key20Sec, new Bar(key20Sec).put(ticker))
        momentum20.update(DateUtil.keyOfUnitSeconds(now.minusSeconds(20), 20))
        momentum20.clean(DateUtil.keyOfUnitSeconds(now.minusHours(dataKeepTime), 20))
      }
    }

    val key30Sec = DateUtil.keyOfUnitSeconds(now, 30)
    candles30sec.get(key30Sec) match {
      case Some(v) => v.put(ticker)
      case _ => candles30sec.put(key30Sec, new Bar(key30Sec).put(ticker))
    }
  }


  def processEvery1minutes(): Unit = {
    val now = DateUtil.now()

    def keyOfBefore(min:Int) =
      DateUtil.keyOfUnitSeconds(now.minus(min, ChronoUnit.MINUTES), 10)
    
    val key60 = keyOfBefore(60)

    def cleanCandle(candle:mutable.LinkedHashMap[Long, Bar], refKey: Long): Unit = {
      candle.keys.filter(key => key < refKey).foreach(key => candle.remove(key))
    }

    cleanCandle(candles10sec, key60)
    cleanCandle(candles20sec, key60)
    cleanCandle(candles30sec, key60)

    val key1 = DateUtil.keyOfUnit1Minutes(now.minusHours(dataKeepTime))
    cleanCandle(candles1min, key1)

//    val c4h = candles1min.values
//    val c2h = c4h.takeRight(120)
    val c1h = candles1min.values
    val c20m = c1h.takeRight(20)
    val c10m = c20m.takeRight(10)
//    box4h = Some(Box.of(c4h.toSeq, 240 * 60))
//    box2h = Some(Box.of(c2h.toSeq, 120 * 60))
    box1h = Some(Box.of(c1h.toSeq, 60 * 60))
    box20min = Some(Box.of(c20m.toSeq, 20 * 60))
    box10min = Some(Box.of(c10m.toSeq, 10 * 60))
  }
}
