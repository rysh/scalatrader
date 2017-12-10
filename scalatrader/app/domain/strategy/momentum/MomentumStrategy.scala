package domain.strategy.momentum

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import domain.models
import domain.strategy.{Strategies, Strategy}
import repository.model.scalatrader.User
import domain.Side._
import domain.margin.Margin
import domain.models.Ordering
import domain.strategy.core.Bar
import domain.time.DateUtil

import scala.collection.mutable

class MomentumStrategy(user: User) extends Strategy {
  override def email: String = user.email
  override def key = user.api_key
  override def secret = user.api_secret

  val core = new MomentumCore
  var position: Option[Ordering] = None
  def entry(o: Ordering): Ordering = {
    position = Some(o)
    o
  }
  def close = {
    position = None
  }

  var leverage = Margin.defaltLeverage
  var orderSize: Double = Margin.defaultSizeUnit * leverage
  override def judgeByTicker(ticker: models.Ticker): Option[Ordering] = {
    val duration = 10
    val now = ZonedDateTime.parse(ticker.timestamp)
    def key(num:Int) = DateUtil.keyOf(now.minus(num * duration, ChronoUnit.SECONDS), duration)
    val key1 = key(1)
    val key2 = key(2)
    val key3 = key(3)
    val key4 = key(4)
    val key11 = key(11)
    val key12 = key(12)
    val key13 = key(13)
    val key14 = key(14)
    judge(Strategies.coreData.candles10sec.values, key1, key2, key3, key4, key11, key12, key13, key14)
  }

  override def judgeEveryMinutes(key: Long): Option[Ordering] = {
    return None
    //judge(Strategies.coreData.candles1min, key - 1, key - 2, key - 3, key - 4)
  }

  private def judge(candles: mutable.LinkedHashMap[Long, Bar], key1: Long, key2: Long, key3: Long, key4: Long, key11: Long, key12: Long, key13: Long, key14: Long) = {
    val res: Option[Ordering] = for {
      b1 <- candles.get(key1)
      b2 <- candles.get(key2)
      b3 <- candles.get(key3)
      b4 <- candles.get(key4)
      b11 <- candles.get(key11)
      b12 <- candles.get(key12)
      b13 <- candles.get(key13)
      b14 <- candles.get(key14)
      m1 = b11.close - b1.close
      m2 = b12.close - b2.close
      m3 = b13.close - b3.close
      m4 = b14.close - b4.close
    } yield {
      if (position.isEmpty) {
        if (m1 > 0 && m2 > 0 && m3 < 0 && m4 < 0) {
          entry(Ordering(Buy, orderSize))
        } else if (m1 < 0 && m2 < 0 && m3 > 0 && m4 > 0) {
          entry(Ordering(Sell, orderSize))
        } else {
          Ordering(Buy, 0)
        }
      } else {
        val isBuy = position.exists(_.side == Buy)
        if (isBuy && m1 < 0 && m2 < 0) {
          close
          Ordering(Sell, orderSize)
        } else if (!isBuy && m1 > 0 && m2 > 0) {
          close
          Ordering(Buy, orderSize)
        } else {
          Ordering(Buy, 0)
        }
      }
    }
    res.filter(_.size > 0)
  }

  override def putTicker(ticker: models.Ticker): Unit = {
  }

  override def processEvery1minutes() = {
  }

  override def init(): Unit = close
}
