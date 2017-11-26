package application

import java.time.temporal.ChronoUnit

import adapter.BitFlyer
import akka.actor.Actor
import com.google.inject.Inject
import domain.ProductCode
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
    if (values.size > 0) {
      val in20 = Some(Bar.of(values))
      val values10 = values.filter(b => key10 <= b.key)
      if (values10.size > 0) {
        bar_10min = Some(Bar.of(values10))
      }
      bar_20min = in20
    }

    //bar_10min.map(f => println(s"bar_10min:${f}"))
    //bar_20min.map(f => println(s"bar_20min:${f}"))
//    println("candles1min")
    //values.lastOption.map(println)
    //println(positionByUser)
  }


  def updatePosition(): Unit = {
    if (domain.isBackTesting) return
    Future {
      UserRepository.everyoneWithApiKey(secret)
        .foreach(user => {
          try {
            //val col: Collateral = BitFlyer.getCollateral(user.api_key, user.api_secret)
            val position = BitFlyer.getPosition(ProductCode.btcFx, user.api_key, user.api_secret)
            position.map(p => TurtleCore.positionByUser.put(user.email, p))
            if (position.isEmpty) {
              TurtleCore.positionByUser.remove(user.email)
            }

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