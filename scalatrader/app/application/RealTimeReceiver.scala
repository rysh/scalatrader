package application

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Named

import adapter.aws.S3
import adapter.bitflyer.PubNubReceiver
import akka.actor.ActorRef
import com.amazonaws.regions.Regions
import com.google.gson.{JsonElement, Gson}
import com.google.inject.{Inject, Singleton}
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.{PNPresenceEventResult, PNMessageResult}
import domain.ProductCode
import domain.models.Ticker
import domain.strategy.turtle.TurtleCore.candles1min
import domain.strategy.turtle.{Bar, TurtleCore, TurtleStrategy}
import domain.time.DateUtil
import play.api.Configuration
import repository.UserRepository

import scala.concurrent.Future

@Singleton
class RealTimeReceiver @Inject()(config: Configuration, @Named("candle") candleActor: ActorRef) {
  print("init RealTimeReceiver")
  val secret = config.get[String]("play.http.secret.key")

  lazy val users = UserRepository.everyoneWithApiKey(secret)
  lazy val turtleStrategy = new TurtleStrategy(users, candleActor)

  val gson: Gson = new Gson()

  val productCode = s"lightning_ticker_${ProductCode.btcFx}"
  val key =  "sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f"
  val callback = new SubscribeCallback() {
    override def message(pubnub: PubNub, message: PNMessageResult) = {
      val ticker: Ticker = gson.fromJson(message.getMessage, classOf[Ticker])
      turtleStrategy.exec(ticker.ltp)
      TurtleCore.put(ticker)
    }
    override def presence(pubnub: PubNub, presence: PNPresenceEventResult) = {
      println("RealTimeReceiver#presence")
      println(presence)
    }

    override def status(pubnub: PubNub, status: PNStatus) = {
      println("RealTimeReceiver#status")
      println(status)
    }
  }

  PubNubReceiver.start(productCode,key, callback)
  println("PubNubReceiver started")

  val s3 = S3.create(Regions.US_WEST_1)
  import DateUtil._
  for (i <- (1 to 20).reverse) {
    val time = now().minus(i, ChronoUnit.MINUTES)
    val s3Path: String = format(time, "yyyy/MM/dd/HH/mm")
    s3.getLines("btcfx-ticker-scala",s3Path).foreach(json => {

      val ticker: Ticker = gson.fromJson(json, classOf[Ticker])
      val key = keyOfUnit1Minutes(time)
      candles1min.get(key) match {
        case Some(v) => v.put(ticker.ltp)
        case _ => candles1min.put(key, new Bar(key))
      }
    })
  }

}