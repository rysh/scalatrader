package domain.strategy.momentum

import java.time.ZonedDateTime

import domain.Side.{Sell, Buy}
import domain.margin.Margin
import domain.models.{Ticker, Ordering}
import domain.strategy.core.Momentum
import domain.models
import domain.strategy.{Strategies, Strategy}
import domain.time.DateUtil
import repository.model.scalatrader.User


class MomentumStrategy(user: User) extends Strategy {
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
  }

  var position: Option[Ordering] = None
  var entryTime: Option[ZonedDateTime] = None
  val stopRange:Option[Double] = None
  var losLimit:Option[Double] = None

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {

    val momentum5min = Strategies.coreData.momentum5min
    val latestOption = momentum5min.latest
    val previousOption = momentum5min.oneFromLast
    val ltp = ticker.ltp
    val macd = Strategies.coreData.macd5m

    val result = if (!isAvailable || latestOption.isEmpty || previousOption.isEmpty || macd.isEmpty()) {
      None
    } else {
      val latest = latestOption.get._2
      val previous = previousOption.get._2
      if (position.isEmpty) {
        if (previous < 0 && latest > 0 && ((previous - latest).abs > 2000) && macd.buySignal) {
          losLimit = stopRange.map(ltp - _)
          entry(Ordering(Buy, orderSize, true))
        } else if (previous > 0 && latest < 0 && ((previous - latest).abs > 2000) && macd.sellSignal) {
          losLimit = stopRange.map(ltp + _)
          entry(Ordering(Sell, orderSize, true))
        } else {
          None
        }
      } else  {
        if (position.get.side == Sell) {
          if (losLimit.exists(_ < ltp)) {
            close()
            Some(Ordering(Buy, position.map(_.size).getOrElse(orderSize), false))
          } else if (latest > 0) {
            close()
            Some(Ordering(Buy, position.map(_.size).getOrElse(orderSize), false))
          } else if (false) {
            losLimit = stopRange.map(ltp + _)
            None
          } else {
            None
          }
        } else { // BUY
          if (losLimit.exists(ltp < _)) {
            close()
            Some(Ordering(Sell, position.map(_.size).getOrElse(orderSize), false))
          } else if (latest < 0) {
            close()
            Some(Ordering(Sell, position.map(_.size).getOrElse(orderSize), false))
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