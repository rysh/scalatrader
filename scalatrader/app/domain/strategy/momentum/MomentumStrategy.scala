package domain.strategy.momentum

import java.time.ZonedDateTime

import domain.models
import domain.strategy.{Strategy, Strategies}
import repository.model.scalatrader.User
import domain.Side._

class MomentumStrategy(user: User) extends Strategy {
  override def email: String = user.email

  val sizeUnit = 0.2
  override def putTicker(ticker: models.Ticker): Unit = {

  }

  override def judgeEveryMinutes(key: Long): Option[(String, Double)] = {
    import Strategies.coreData._
    val position = Strategies.getPosition(user.email)
    val res: Option[(String, Double)] = for {
      m1 <- candles1min.get(key - 1)
      m2 <- candles1min.get(key - 2)
      m3 <- candles1min.get(key - 3)
      m4 <- candles1min.get(key - 4)
    } yield {
      if (position.isEmpty) {
        if (m1.high < m2.high && m2.high < m3.high && m3.high < m4.high) {
          println(s"m1.high(${m1.high}) < m2.high(${m2.high}) < m3.high(${m3.high}) < m4.high(${m4.high})")
          (Buy, sizeUnit)
        } else if (m1.low > m2.low && m2.low > m3.low && m3.low > m4.low) {
          (Sell, sizeUnit)
        } else {
          (Buy, 0)
        }
      } else {
        val isBuy = position.map(_.side == Buy).getOrElse(false)
        if (!isBuy && (m1.high < m2.high && m2.high < m3.high)) {
          (Buy, sizeUnit)
        } else if (isBuy && (m1.low > m2.low && m2.low > m3.low)) {
          (Sell, sizeUnit)
        } else {
          (Buy, 0)
        }
      }
    }
    res.filter(a => a._2 > 0)
  }

  override def loadInitialData(initialData: Seq[(Long, Iterator[String])]): Unit = {

  }
}
