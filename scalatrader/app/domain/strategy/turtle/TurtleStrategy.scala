package domain.strategy.turtle

import javax.inject.Named

import adapter.BitFlyer
import akka.actor.ActorRef
import com.google.inject.Inject
import domain.Side
import repository.UserRepository

@Singleton
class TurtleStrategy @Inject() (@Named("candle") candleActor: ActorRef) {
  val sizeUnit = 0.2
  def exec(ltp: Double, secret: String): Unit = {
    synchronized {
      check(ltp).map(side => {
        UserRepository.everyoneWithApiKey(secret).foreach(user => {
          BitFlyer.orderByMarket(side, sizeUnit, user.api_key, user.api_secret)
          candleActor ! "updatePosition"
        })
      })
    }
  }
  def check(ltp: Double): Option[String] = {
    import TurtleCore._
    if (bar_10min.isEmpty) return None
    if (bar_20min.isEmpty) return None

    val bar10 = bar_10min.get
    val bar20 = bar_20min.get

    if (position.isEmpty || position.get.size < (sizeUnit / 2)) {
      if (bar20.high < ltp) {
        Some(Side.Buy)
      } else if (ltp < bar20.low) {
        Some(Side.Sell)
      } else {
        None
      }
    } else if (position.get.side == Side.Sell) {
      if (bar10.high < ltp) {
        Some(Side.Buy)
      } else {
        None
      }
    } else { // BUY
      if (ltp < bar10.high) {
        Some(Side.Sell)
      } else {
        None
      }
    }
  }
}