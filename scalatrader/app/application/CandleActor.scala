package application

import adapter.BitFlyer
import akka.actor.Actor
import com.google.inject.Inject
import domain.ProductCode
import domain.margin.Margin
import domain.models.{Execution, Collateral, Positions}
import domain.strategy.Strategies
import play.api.{Configuration, Logger}
import repository.UserRepository
import repository.model.scalatrader.User


class CandleActor @Inject()(config: Configuration) extends Actor {
  val secret = config.get[String]("play.http.secret.key")

  def receive = {
    case "1min" => exec()
    case _ => println()
  }

  def exec(): Unit = {
    Strategies.processEvery1minutes()
//    val users = UserRepository.everyoneWithApiKey(secret)
//    updateMargin(users)
  }

  def updateMargin(users: Seq[User]): Unit = {
    users.foreach(user => {
      try {
        //TODO Errorの原因を確認
//        val latest: Execution = BitFlyer.getLatestExecution()
//        val col: Collateral = BitFlyer.getCollateral(user.api_key, user.api_secret)
//        val pos: Positions = BitFlyer.getPositions(user.api_key, user.api_secret)
//        Margin.sizeUnit = new Margin(col.collateral - col.open_position_pnl, pos, latest.price).sizeOf1x
      } catch {
        case e: Exception => {
          e.printStackTrace()
          Logger.error(s"updateMargin error: ${user.email}", e)
        }
      }
    })
  }
}