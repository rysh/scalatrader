package application

import java.time.temporal.ChronoUnit

import adapter.BitFlyer
import akka.actor.Actor
import com.google.inject.Inject
import domain.strategy.turtle.{Bar, TurtleCore}
import domain.time.DateUtil
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
    val now = DateUtil.now()

    def keyOfBefore(min:Int) =
      DateUtil.keyOfUnit1Minutes(now.minus(min, ChronoUnit.MINUTES))

    val key20 = keyOfBefore(20)
    val key10 = keyOfBefore(10)

    import TurtleCore._
    candles1min.keys.filter(key => key < key20).foreach(key => candles1min.remove(key))

    val values = candles1min.values.toSeq.sortBy(_.key)
    println(values)
    val in20 = Some(Bar.of(values))
    val in10 = Some(Bar.of(values.filter(b => key10 <= b.key)))

    bar_20min = in20
    bar_10min = in10

    println("bar_10min")
    println(in10)
    println("bar_20min")
    println(bar_20min)
    println("candles1min")
    candles1min.values.toSeq.sortBy(_.key).foreach(println)
  }


  def updatePosition() = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Future {
      UserRepository.everyoneWithApiKey(secret)
        .foreach(user => {
          try {
            //val col: Collateral = BitFlyer.getCollateral(user.api_key, user.api_secret)
            TurtleCore.position = BitFlyer.getPositions(user.api_key, user.api_secret).btcFx

          } catch {
            case e: Exception => {
              e.printStackTrace()
              Logger.error(s"updatePosition error: ${user.email}", e)
            }
          }
        })
    }
  }
}