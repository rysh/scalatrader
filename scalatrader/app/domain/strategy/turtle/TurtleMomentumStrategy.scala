package domain.strategy.turtle

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import domain.Side.{Sell, Buy}
import domain.margin.Margin
import domain.models.{Ticker, Ordering}
import domain.{Side, models}
import domain.strategy.{Strategy, Strategies}
import domain.time.DateUtil
import repository.model.scalatrader.User


class TurtleMomentumStrategy(user: User) extends Strategy {
  override def putTicker(ticker: models.Ticker) = {
    core.put(ticker)
  }

  val core = new TurtleCore
  override def email = user.email
  override def key = user.api_key
  override def secret = user.api_secret

  var leverage = 2.0
  var orderSize = Margin.defaultSizeUnit * leverage
  var position: Option[Ordering] = None
  def entry(o: Ordering): Option[Ordering] = {
    position = Some(o)
    updateSizeUnit
    position
  }
  def close = {
    position = None
    losLimit = None
  }

  val stopRange:Option[Double] = Some(3000)
  var losLimit:Option[Double] = None
  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {
    val ltp = ticker.ltp

    val result = if (!isAvailable || core.candles10sec.size < 22) {
      None
    } else {
      val box10 = core.box10sec.get
      val box20 = core.box20sec.get
      if (position.isEmpty) {
        if (box20.high < ltp) {
          losLimit = None
          entry(Ordering(Side.Buy, orderSize))
        } else if (ltp < box20.low) {
          losLimit = None
          entry(Ordering(Side.Sell, orderSize))
        } else {
          None
        }
      } else  {
        val candles = Strategies.coreData.candles10sec
        val duration = 10
        val now = ZonedDateTime.parse(ticker.timestamp)
        def key(num:Int) = DateUtil.keyOfUnitSeconds(now.minus(num * duration, ChronoUnit.SECONDS), duration)
        val key1 = key(1)
        val key2 = key(2)
        val key3 = key(3)
        val key11 = key(4)
        val key12 = key(5)
        val key13 = key(6)
        val res: Option[Ordering] = for {
          b1 <- candles.get(key1)
          b2 <- candles.get(key2)
          b3 <- candles.get(key3)
          b11 <- candles.get(key11)
          b12 <- candles.get(key12)
          b13 <- candles.get(key13)
          m1 = b11.close - b1.close
          m2 = b12.close - b2.close
        } yield {
          val sizeUnit = position.map(_.size).getOrElse(orderSize)
          val isBuy = position.exists(_.side == Buy)
          if (isBuy && m1 < 0 && m2 < 0) {
            close
            Ordering(Sell, sizeUnit)
          } else if (!isBuy && m1 > 0 && m2 > 0) {
            close
            Ordering(Buy, sizeUnit)
          } else {
            Ordering(Buy, 0)
          }
        }
        res.filter(_.size > 0)
      }
    }
    result
  }

  private def updateSizeUnit = {
    val newSize = Margin.sizeUnit * leverage
    if (orderSize < newSize) {
      println(s"orderSize($orderSize) -> newSize($newSize)")
      orderSize = newSize
    }
  }


  override def processEvery1minutes(): Unit = {
    core.refresh()
  }

  override def init(): Unit = {
    position = None
    losLimit = None
    core.init()
    leverage = 2.0
    orderSize = 0.2 * leverage
  }
}