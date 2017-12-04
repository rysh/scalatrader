package application

import adapter.BitFlyer
import akka.actor.Actor
import com.google.inject.Inject
import domain.ProductCode
import domain.strategy.Strategies
import play.api.{Configuration, Logger}
import repository.UserRepository

import scala.concurrent.Future


class CandleActor @Inject()(config: Configuration) extends Actor {
  val secret = config.get[String]("play.http.secret.key")

  def receive = {
    case "1min" => exec()
    case "updatePosition" => updatePosition()
    case _ => println()
  }

  def exec(): Unit = {
    updatePosition()
    Strategies.processEvery1minutes()
  }


  def updatePosition(): Unit = {
    if (domain.isBackTesting) return
    Future {
      UserRepository.everyoneWithApiKey(secret)
        .foreach(user => {
          try {
            //val col: Collateral = BitFlyer.getCollateral(user.api_key, user.api_secret)
            val position = BitFlyer.getPosition(ProductCode.btcFx, user.api_key, user.api_secret)
            Strategies.coreData.updatePosition(user, position)

          } catch {
            case e: Exception => {
              e.printStackTrace()
              Logger.error(s"updatePosition error: ${user.email}", e)
            }
          }
        })
    } (scala.concurrent.ExecutionContext.Implicits.global)
  }
}