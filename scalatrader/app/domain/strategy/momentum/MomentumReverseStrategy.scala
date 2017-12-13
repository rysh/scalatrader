package domain.strategy.momentum

import java.time.ZonedDateTime

import domain.Side.{Sell, Buy}
import domain.margin.Margin
import domain.models.{Ticker, Ordering}
import domain.models
import domain.strategy.{Strategies, Strategy}
import domain.time.DateUtil
import repository.model.scalatrader.User


class MomentumReverseStrategy(user: User) extends Strategy {
  override def putTicker(ticker: models.Ticker): Unit = {
  }

  override def email: String = user.email
  override def key: String = user.api_key
  override def secret: String = user.api_secret

  var leverage = Margin.defaltLeverage
  var orderSize: Double = Margin.defaultSizeUnit * leverage

  def entry(o: Ordering): Option[Ordering] = {
    position = Some(o)
    updateSizeUnit()
    entryTime = Some(DateUtil.now())
    position
  }
  def close(): Unit = {
    position = None
    losLimit = None
    entryTime = None
    top = None
    bottom = None
  }

  var position: Option[Ordering] = None
  var entryTime: Option[ZonedDateTime] = None
  val stopRange:Option[Double] = None
  var losLimit:Option[Double] = None
  var top:Option[Double] = None
  var bottom:Option[Double] = None

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {

    val momentum5min = Strategies.coreData.momentum5min
    val momentum = momentum5min.values.values.takeRight(3).toSeq
    val ltp = ticker.ltp

    val result = if (!isAvailable || momentum.size < 3) {
      None
    } else {
      val one = momentum.head
      val two = momentum.tail.head
      val three = momentum.last
      if (one < two && two > three) {
        top = Some(two)
      } else if (one > two && two < three) {
        bottom = Some(two)
      }
      if (position.isEmpty) {
        if (one < two && two > three) {
//        if (top.exists(t => (t - 5000) > three) && box20m.exists(_.isUp)) {
          losLimit = stopRange.map(ltp - _)
          entry(Ordering(Buy, orderSize))
        } else if (one > two && two < three) {
//          } else if (bottom.exists(b => (b + 5000) < three) && box20m.exists(_.isDown)) {
          losLimit = stopRange.map(ltp + _)
          entry(Ordering(Sell, orderSize))
        } else {
          None
        }
      } else  {
        if (position.get.side == Sell) {
          if (losLimit.exists(_ < ltp)) {
            close()
            Some(Ordering(Buy, position.map(_.size).getOrElse(orderSize)))
          } else if (one < two && two > three) {
            close()
            Some(Ordering(Buy, position.map(_.size).getOrElse(orderSize)))
          } else if (false) {
            losLimit = stopRange.map(ltp + _)
            None
          } else {
            None
          }
        } else { // BUY
          if (losLimit.exists(ltp < _)) {
            close()
            Some(Ordering(Sell, position.map(_.size).getOrElse(orderSize)))
          } else if (one > two && two < three) {
            close()
            Some(Ordering(Sell, position.map(_.size).getOrElse(orderSize)))
          } else if (false) {
            losLimit = stopRange.map(ltp - _)
            None
          } else {
            None
          }
        }
      }
    }
    result
  }

  private def updateSizeUnit(): Unit = {
    val newSize = Margin.sizeUnit * leverage
    if (orderSize < newSize) {
      println(s"orderSize($orderSize) -> newSize($newSize)")
      orderSize = newSize
    }
  }


  override def processEvery1minutes(): Unit = {

  }

  override def init(): Unit = {
    position = None
    losLimit = None
    leverage = Margin.leverage
    orderSize = Margin.sizeUnit * leverage
  }
}