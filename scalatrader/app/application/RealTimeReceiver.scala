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
import domain.strategy.momentum.MomentumReverseStrategy
import domain.strategy.turtle.TurtleStrategy
import domain.time.DateUtil
import play.api.Configuration
import repository.UserRepository

import scala.concurrent.Future

@Singleton
class RealTimeReceiver @Inject()(config: Configuration, @Named("candle") candleActor: ActorRef) {
  println("init RealTimeReceiver")
  val secret = config.get[String]("play.http.secret.key")

  def start: Unit = {
    lazy val users = UserRepository.everyoneWithApiKey(secret)
    if (users.isEmpty) return
    users.filter(user => !Strategies.values.exists(_.email == user.email))
      .map(user => new MomentumReverseStrategy(user))
      .foreach(Strategies.register)


    val gson: Gson = new Gson()

    val productCode = s"lightning_ticker_${ProductCode.btcFx}"
    val key =  "sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f"
    val callback = new SubscribeCallback() {
      override def message(pubnub: PubNub, message: PNMessageResult) = {
        val ticker: Ticker = gson.fromJson(message.getMessage, classOf[Ticker])
        Strategies.values.filter(_.isAvailable)foreach(strategy => {
          strategy.synchronized {
            strategy.judgeByTicker(ticker).foreach(ordering => {
              val order: models.Order = Orders.market(ordering)
              println(s"[order][${order.side}][${ticker.timestamp}] price:${ticker.ltp.toLong} size:${order.size}")
              Future {
                val response = BitFlyer.orderByMarket(order, strategy.key, strategy.secret)
                if (ordering.isEntry) {
                  strategy.orderId = Some(response.child_order_acceptance_id)
                  UserRepository.storeCurrentOrder(strategy.email, response.child_order_acceptance_id, order.side, order.size)
                } else {
                  strategy.orderId = None
                  UserRepository.clearCurrentOrder(strategy.email, response.child_order_acceptance_id)
                }
              } (scala.concurrent.ExecutionContext.Implicits.global)
            })
          }
        })
        Strategies.putTicker(ticker)
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
//        val initialData: Seq[Ticker] = InitialDataLoader.loadFromS3()
        val initialData: Seq[Ticker] = DataLoader.loadFromLocal()
        initialData.foreach(ticker => {
          Strategies.coreData.putTicker(ticker)
          Strategies.values.foreach(_.putTicker(ticker))
        })
//        Strategies.coreData.momentum10.loadAll()
//        Strategies.coreData.momentum20.loadAll()
//        Strategies.coreData.momentum1min.loadAll()
//        Strategies.coreData.momentum5min.loadAll()
        Strategies.processEvery1minutes()
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