package domain.strategy.turtle

import domain.models.Ticker
import domain.{Side, models}
import domain.strategy.{Strategies, Strategy}
import repository.model.scalatrader.User

class TurtleStrategy(user: User) extends Strategy {
  override def putTicker(ticker: models.Ticker) = core.put(ticker)
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

    val result = if (!available || core.bar_10min.isEmpty || core.bar_20min.isEmpty) {
      None
    } else {
      val bar10 = core.bar_10min.get
      val bar20 = core.bar_20min.get
      if (position.isEmpty || position.get.size < (sizeUnit / 2)) {
        if (bar20.high < ltp) {
          println("bar20.high < ltp")
          losLimit = None
          Some((Side.Buy, sizeUnit))
        } else if (ltp < bar20.low) {
          println(s"ltp($ltp) < bar20.low(${bar20.low})")
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
        } else if (bar10.high < ltp) {
          println("bar10.high < ltp")
          losLimit = None
          Some((Side.Buy, sizeUnit))
        } else if (ltp < bar20.low) {
          println(s"ltp($ltp) < bar20.low(${bar20.low}) not order because already have position")
          losLimit = Some(ltp + 3000)
          None
        } else {
          None
        }
      } else { // BUY
        if (losLimit.map(limit => ltp < limit).getOrElse(false)) {
          println(s"ltp($ltp) < limit(${losLimit.get})")
          losLimit = None
          Some((Side.Sell, sizeUnit))
        } else if (ltp < bar10.low) {
          println(s"ltp($ltp) < bar10.low(${bar10.low})")
          losLimit = None
          Some((Side.Sell, sizeUnit))
        } else if (bar20.high < ltp) {
          println(s"bar20.high(${bar20.high}) < ltp($ltp) not order because already have position")
          losLimit = Some(ltp - 3000)
          None
        } else {
          None
        }
      }
    }
    core.put(ticker)
    result
  }

  override def processEvery1minutes() = {
    core.refresh();
  }
}