package domain.strategy.turtle

import domain.models.Position
import domain.Side
import repository.model.scalatrader.User

class TurtleStrategy(user: User) {
  def email = user.email

  val sizeUnit = 0.2

  var losLimit:Option[Double] = None
  def check(ltp: Double): Option[(String, Double)] = {
    import TurtleCore._
    val position = positionByUser.get(user.email)
    if (bar_10min.isEmpty) return None
    if (bar_20min.isEmpty) return None

    val bar10 = bar_10min.get
    val bar20 = bar_20min.get

    if (position.isEmpty || position.get.size < (sizeUnit / 2)) {
      if (bar20.high < ltp) {
        println("bar20.high < ltp")
        Some((Side.Buy, sizeUnit))
      } else if (ltp < bar20.low) {
        println(s"ltp($ltp) < bar20.low(${bar20.low})")
        Some((Side.Sell, sizeUnit))
      } else {
        None
      }
    } else if (position.get.side == Side.Sell) {
      if (losLimit.map(limit => limit < ltp).getOrElse(false)) {
        println(s"limit(${losLimit.get}) < ltp($ltp)")
        Some((Side.Buy, sizeUnit))
      } else if (bar10.high < ltp) {
        println("bar10.high < ltp")
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
        Some((Side.Sell, sizeUnit))
      } else if (ltp < bar10.low) {
        println(s"ltp($ltp) < bar10.low(${bar10.low})")
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
}