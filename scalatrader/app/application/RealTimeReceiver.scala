package application

import java.time.temporal.ChronoUnit
import javax.inject.Named

import adapter.BitFlyer
import adapter.aws.S3
import adapter.bitflyer.PubNubReceiver
import akka.actor.ActorRef
import com.amazonaws.regions.Regions
import com.google.gson.Gson
import com.google.inject.{Inject, Singleton}
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.{PNPresenceEventResult, PNMessageResult}
import domain.{ProductCode, models}
import domain.models.{Ticker, Orders}
import domain.strategy.Strategies
import domain.strategy.turtle.TurtleStrategy
import domain.time.DateUtil
import play.api.Configuration
import repository.UserRepository

import scala.concurrent.Future

@Singleton
class RealTimeReceiver @Inject()(config: Configuration, @Named("candle") candleActor: ActorRef) {
  print("init RealTimeReceiver")
  val secret = config.get[String]("play.http.secret.key")

  def start: Unit = {
    lazy val users = UserRepository.everyoneWithApiKey(secret)
    if (users.isEmpty) return ()
    users.map(user => new TurtleStrategy(user)).foreach(st => {
      if (!Strategies.values.exists(_.email == st.email)) {
        Strategies.register(st)
      }
    })

    val gson: Gson = new Gson()

    val productCode = s"lightning_ticker_${ProductCode.btcFx}"
    val key =  "sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f"
    val callback = new SubscribeCallback() {
      override def message(pubnub: PubNub, message: PNMessageResult) = {
        val ticker: Ticker = gson.fromJson(message.getMessage, classOf[Ticker])
        Strategies.values.filter(_.isAvailable)foreach(strategy => {
          strategy.synchronized {
            strategy.judgeByTicker(ticker).map(Orders.market).foreach((order: models.Order) => {
              println(s"[order][${order.side}][${ticker.timestamp}] price:${ticker.ltp.toLong} size:${order.size}")
              Future {
                BitFlyer.orderByMarket(order, strategy.key, strategy.secret)
              } (scala.concurrent.ExecutionContext.Implicits.global)
            })
          }
        })
        Strategies.values.foreach(_.putTicker(ticker))
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
    def loadInitialData() = {
      Future{
        val s3 = S3.create(Regions.US_WEST_1)
        val gson: Gson = new Gson
        import DateUtil._
        val initialData = (1 to 60).reverse.par.flatMap(i => {
          val time = now().minus(i, ChronoUnit.MINUTES)
          val s3Path: String = format(time, "yyyy/MM/dd/HH/mm")
          val key = keyOfUnit1Minutes(time)
          s3.getLines("btcfx-ticker-scala", s3Path)
        }).map(json => gson.fromJson(json, classOf[Ticker]))
        initialData.foreach(ticker => {
          Strategies.coreData.putTicker(ticker)
          Strategies.values.foreach(_.putTicker(ticker))
        })
        Strategies.coreData.momentum10.loadAll()
        Strategies.values.foreach(st => st.availability.initialDataLoaded = true)
      } (scala.concurrent.ExecutionContext.Implicits.global)
    }
    loadInitialData()
  }
  Future {
    Thread.sleep(10 * 1000)
    if (!domain.isBackTesting) {
      start
    }
  } (scala.concurrent.ExecutionContext.Implicits.global)
}