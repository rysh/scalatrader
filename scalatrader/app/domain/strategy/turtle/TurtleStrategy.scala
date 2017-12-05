package domain.strategy.turtle

import domain.models.Ticker
import domain.strategy.core.{Bar, Box}
import domain.{Side, models}
import domain.strategy.{Strategies, Strategy}
import repository.model.scalatrader.User

class TurtleStrategy(user: User) extends Strategy {
  override def putTicker(ticker: models.Ticker) = {
    core.put(ticker)
  }
  var available = false

  override def loadInitialData(initialData: Seq[(Long, Iterator[String])]): Unit = {
    core.loadInitialData(initialData)
    available = true
  }

  val core = new TurtleCore
  override def email = user.email

  val sizeUnit = 0.2

  var losLimit:Option[Double] = None
  override def judgeByTicker(ticker: Ticker): Option[(String, Double)] = {
    val ltp = ticker.ltp
    val position = Strategies.getPosition(user.email)

    val result = if (!available || core.candles10sec.size < 22) {
      None
    } else {
      val box10 = core.box10sec.get
      val box20 = core.box20sec.get

      if (position.isEmpty || position.get.size < (sizeUnit / 2)) {
        if (box20.high < ltp) {
          println("box20.high < ltp")
          losLimit = None
          Some((Side.Buy, sizeUnit))
        } else if (ltp < box20.low) {
          println(s"ltp($ltp) < box20.low(${box20.low})")
          losLimit = None
          Some((Side.Sell, sizeUnit))
        } else {
          None
        }
      } else if (position.get.side == Side.Sell) {
        if (losLimit.map(limit => limit < ltp).getOrElse(false)) {
          println(s"limit(${losLimit.get}) < ltp($ltp)")
          losLimit = None
          Some((Side.Buy, sizeUnit))
        } else if (box10.high < ltp) {
          println("box10.high < ltp")
          losLimit = None
          Some((Side.Buy, sizeUnit))
        } else if (ltp < box20.low) {
          println(s"ltp($ltp) < box20.low(${box20.low}) not order because already have position")
          losLimit = Some(ltp + 3000)
          None
        } else {
          None
        }
      } else { // BUY
        if (losLimit.exists(limit => ltp < limit)) {
          println(s"ltp($ltp) < limit(${losLimit.get})")
          losLimit = None
          Some((Side.Sell, sizeUnit))
        } else if (ltp < box10.low) {
          println(s"ltp($ltp) < box10.low(${box10.low})")
          losLimit = None
          Some((Side.Sell, sizeUnit))
        } else if (box20.high < ltp) {
          println(s"box20.high(${box20.high}) < ltp($ltp) not order because already have position")
          losLimit = Some(ltp - 3000)
          None
        } else {
          None
        }
      }
    }
    result
  }

  override def processEvery1minutes(): Unit = {
    core.refresh()
  }
}