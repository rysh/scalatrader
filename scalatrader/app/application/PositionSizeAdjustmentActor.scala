package application

import adapter.BitFlyer
import akka.actor.Actor
import com.google.inject.Inject
import domain.margin.Margin
import domain.strategy.Strategies
import play.api.{Configuration, Logger}
import repository.UserRepository


class PositionSizeAdjustmentActor @Inject()(config: Configuration) extends Actor {
  val secret: String = config.get[String]("play.http.secret.key")

  def receive: PartialFunction[Any, Unit] = {
    case "" => updateMargin()
    case _ => println()
  }

  def updateMargin(): Unit = {
    Strategies.processEvery1minutes()
    val users = UserRepository.everyoneWithApiKey(secret)
    users.foreach(user => {
      try {
        val latest = BitFlyer.getLatestExecution()
        val col = BitFlyer.getCollateral(user.api_key, user.api_secret)
        val pos = BitFlyer.getPositions(user.api_key, user.api_secret)
        val oldSieUnit = Margin.sizeUnit
        Margin.sizeUnit = new Margin(col.collateral - col.open_position_pnl, pos, latest.price).sizeOf1x
        if (oldSieUnit != Margin.sizeUnit) {
          println(s"sizeUnit updating: $oldSieUnit -> ${Margin.sizeUnit}")
        }
      } catch {
        case e: Exception =>
          e.printStackTrace()
          Logger.error(s"updateMargin error: ${user.email}", e)
      }
    })
  }
}