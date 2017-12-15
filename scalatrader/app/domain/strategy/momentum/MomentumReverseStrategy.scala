package domain.strategy.momentum

import domain.Side.{Sell, Buy}
import domain.margin.Margin
import domain.models.{Ticker, Ordering}
import domain.models
import domain.strategy.{Strategies, Strategy}
import repository.model.scalatrader.User


class MomentumReverseStrategy(user: User) extends Strategy {
  override def putTicker(ticker: models.Ticker): Unit = {
  }

  override def email: String = user.email
  override def key: String = user.api_key
  override def secret: String = user.api_secret

  def entry(o: Ordering): Option[Ordering] = {
    entryPosition = Some(o)
    entryPosition
  }
  def close(): Unit = {
    entryPosition = None
  }

  override def judgeByTicker(ticker: Ticker): Option[Ordering] = {

    val momentum5min = Strategies.coreData.momentum5min
    val momentum = momentum5min.values.values.takeRight(3).toSeq

    val orderSize: Double = Margin.size

    val result = if (!isAvailable || momentum.lengthCompare(3) < 0) {
      None
    } else {
      val one = momentum.head
      val two = momentum.tail.head
      val three = momentum.last

      if (entryPosition.isEmpty) {
        val isEntry = true
        if (one < two && two > three) {
          entry(Ordering(Buy, orderSize, isEntry))
        } else if (one > two && two < three) {
          entry(Ordering(Sell, orderSize, isEntry))
        } else {
          None
        }
      } else  {
        if (entryPosition.get.side == Sell) {
          if (one < two && two > three) {
            close()
            Some(Ordering(Buy, closeSize(orderSize)))
          } else {
            None
          }
        } else { // BUY
          if (one > two && two < three) {
            close()
            Some(Ordering(Sell, closeSize(orderSize)))
          } else {
            None
          }
        }
      }
    }
    result
  }

  private def closeSize(orderSize: Double) =
    entryPosition.map(_.size).getOrElse(orderSize)


  override def processEvery1minutes(): Unit = {

  }

  override def init(): Unit = {
    entryPosition = None
  }
}