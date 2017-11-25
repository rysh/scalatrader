package domain.strategy.turtle

import javax.inject.Named

import adapter.BitFlyer
import akka.actor.ActorRef
import domain.models.Position
import domain.strategy.turtle.BackTestResults.Order
import domain.time.DateUtil
import domain.{Side}
import repository.model.scalatrader.User

class TurtleStrategy(users: Seq[User], @Named("candle") candleActor: ActorRef) {

  val sizeUnit = 0.2

  def exec(ltp: Double): Unit = {
    //TODO ユーザーごとの並行処理にしたい
    users.par.foreach(user => {
      check(ltp, user.email).map(side => {
        val order = BitFlyer.orderByMarket(side, sizeUnit, user.api_key, user.api_secret, true)
        //TODO ユーザーが一人だけなので現状は問題がないが、売買が成立したユーザーだけポジションを更新したい
        candleActor ! "updatePosition"
        if (domain.isBackTesting) {
          BackTestResults.add(Order(order.side, ltp, order.size))
          println(s"注文 ${order.side} price: ${ltp} size:${order.size}")
          val positionToTuple: Position => (String, Double) = (pos: Position) => {
            val posSize: Double = pos.size * (if (pos.side == domain.Side.Sell) -1 else 1)
            val orderSize: Double = order.size * (if (order.side == domain.Side.Sell) -1 else 1)
            val total: Double = posSize + orderSize
            val side: String = if (total < 0) domain.Side.Sell else domain.Side.Buy
            (side, total.abs)
          }
          val maybePosition = TurtleCore.positionByUser.get(user.email)
          val (side: String, size: Double) = if (maybePosition.isEmpty) {
            (order.side, order.size)
          } else {
            positionToTuple(maybePosition.get)
          }
          if (size == 0) {
            TurtleCore.positionByUser.remove(user.email)
          } else {
            val position = Position(domain.ProductCode.btcFx,
              side,
              ltp,
              size, Double.NaN, Double.NaN, Double.NaN, DateUtil.now.toString, Double.NaN, Double.NaN)
            TurtleCore.positionByUser.put(user.email, position)
          }
        }
      })
    })
}
  var losLimit:Option[Double] = None
  def check(ltp: Double, email: String): Option[String] = {
    import TurtleCore._
    if (bar_10min.isEmpty) return None
    if (bar_20min.isEmpty) return None

    val bar10 = bar_10min.get
    val bar20 = bar_20min.get
    val position = positionByUser.get(email)

    if (position.isEmpty || position.get.size < (sizeUnit / 2)) {
      if (bar20.high < ltp) {
        println("bar20.high < ltp")
        Some(Side.Buy)
      } else if (ltp < bar20.low) {
        println(s"ltp($ltp) < bar20.low(${bar20.low})")
        Some(Side.Sell)
      } else {
        None
      }
    } else if (position.get.side == Side.Sell) {
      if (losLimit.map(limit => limit < ltp).getOrElse(false)) {
        println(s"limit(${losLimit.get}) < ltp($ltp)")
        Some(Side.Buy)
      } else if (bar10.high < ltp) {
        println("bar10.high < ltp")
        Some(Side.Buy)
      } else if (ltp < bar20.low) {
        println(s"ltp($ltp) < bar20.low(${bar20.low}) not order because already have position")
        losLimit = Some(ltp)
        None
      } else {
        None
      }
    } else { // BUY
      if (losLimit.map(limit => ltp < limit).getOrElse(false)) {
        println(s"ltp($ltp) < limit(${losLimit.get})")
        Some(Side.Sell)
      } else if (ltp < bar10.low) {
        println(s"ltp($ltp) < bar10.low(${bar10.low})")
        Some(Side.Sell)
      } else if (bar20.high < ltp) {
        println(s"bar20.high(${bar20.high}) < ltp($ltp) not order because already have position")
        losLimit = Some(ltp)
        None
      } else {
        None
      }
    }
  }
}