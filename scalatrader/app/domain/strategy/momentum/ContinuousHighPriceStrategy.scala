package domain.strategy.momentum

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import domain.models
import domain.strategy.{Strategies, Strategy}
import repository.model.scalatrader.User
import domain.Side._
import domain.models.Ordering
import domain.strategy.core.Bar
import domain.time.DateUtil

import scala.collection.mutable

class ContinuousHighPriceStrategy(user: User) extends Strategy {
  override def email: String = user.email
  override def key = user.api_key
  override def secret = user.api_secret

  var position: Option[Ordering] = None
  def entry(o: Ordering): Ordering = {
    position = Some(o)
    o
  }
  def close = {
    position = None
  }

  val sizeUnit = 0.2
  override def judgeByTicker(ticker: models.Ticker): Option[Ordering] = {
    val duration = 10
    val now = ZonedDateTime.parse(ticker.timestamp)
    def key(num:Int) = DateUtil.keyOf(now.minus(num * duration, ChronoUnit.SECONDS), duration)
    val key1 = key(1)
    val key2 = key(2)
    val key3 = key(3)
    val key4 = key(4)
    judge(Strategies.coreData.candles10sec, key1, key2, key3, key4)
  }

  override def judgeEveryMinutes(key: Long): Option[Ordering] = {
    return None
    //judge(Strategies.coreData.candles1min, key - 1, key - 2, key - 3, key - 4)
  }

  private def judge(candles: mutable.LinkedHashMap[Long, Bar], key1: Long, key2: Long, key3: Long, key4: Long): Option[Ordering] = {
    val res: Option[Ordering] = for {
      m1 <- candles.get(key1)
      m2 <- candles.get(key2)
      m3 <- candles.get(key3)
      m4 <- candles.get(key4)
    } yield {
      if (position.isEmpty) {
        if (m1.high < m2.high && m2.high < m3.high && m3.high < m4.high) {
          entry(Ordering(Buy, sizeUnit))
        } else if (m1.low > m2.low && m2.low > m3.low && m3.low > m4.low) {
          entry(Ordering(Sell, sizeUnit))
        } else {
          Ordering(Buy, 0)
        }
      } else {
        val isBuy = position.exists(_.side == Buy)
        if (!isBuy && (m1.high < m2.high && m2.high < m3.high)) {
          close
          Ordering(Buy, sizeUnit)
        } else if (isBuy && (m1.low > m2.low && m2.low > m3.low)) {
          close
          Ordering(Sell, sizeUnit)
        } else {
          Ordering(Buy, 0)
        }
      }
    }
    res.filter(_.size > 0)
  }

  override def putTicker(ticker: models.Ticker): Unit = {

  }

  override def init(): Unit = close
}
