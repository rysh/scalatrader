package application

import javax.inject.Named

import adapter.BitFlyer
import adapter.BitFlyer.OrderResponse
import adapter.aws.{MailContent, SES}
import adapter.bitflyer.PubNubReceiver
import akka.actor.ActorRef
import com.google.gson.Gson
import com.google.inject.{Inject, Singleton}
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.{PNPresenceEventResult, PNMessageResult}
import domain.{ProductCode, models}
import domain.models.{Ticker, Orders}
import domain.strategy.{Strategies, Strategy}
import domain.strategy.momentum.MomentumReverseStrategy
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
                try {
                  var response: OrderResponse = null
                    retry(10, () => {
                      response = BitFlyer.orderByMarket(order, strategy.key, strategy.secret)
                    })
                  if (ordering.isEntry) {
                    strategy.orderId = Some(response.child_order_acceptance_id)
                    UserRepository.storeCurrentOrder(strategy.email, response.child_order_acceptance_id, order.side, order.size)
                  }
                } catch{
                  case e:Exception =>
                    strategy.orderId = None
                    strategy.entryPosition = None
                    if (!ordering.isEntry) {
                      println("!!!close request failed.!!!")
                      sendRequestFailureNoticeMail(strategy, ordering)
                    }
                } finally {
                  if (!ordering.isEntry) {
                    UserRepository.clearCurrentOrder(strategy.email, strategy.orderId.get)
                    strategy.orderId = None
                  }
                }
              } (scala.concurrent.ExecutionContext.Implicits.global)
            })
          }
        })
        Strategies.putTicker(ticker)
      }
      override def presence(pubnub: PubNub, presence: PNPresenceEventResult): Unit = {
        println("RealTimeReceiver#presence")
        println(presence)
      }

      override def status(pubnub: PubNub, status: PNStatus): Unit = {
        println("RealTimeReceiver#status")
        println(status)
      }
    }


    PubNubReceiver.start(productCode,key, callback)
    println("PubNubReceiver started")
    def loadInitialData() = {
      Future{
        val initialData: Seq[Ticker] = DataLoader.loadFromS3()
//        val initialData: Seq[Ticker] = DataLoader.loadFromLocal()
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

  private def sendRequestFailureNoticeMail(strategy: Strategy, ordering: models.Ordering) = {
    val subject = "close request failed"
    val text = s"failed: ${ordering.side} size:${ordering.size}"
    SES.send(MailContent(strategy.email, "info@scalatrader.com", subject, text, text))
  }

  Future {
    Thread.sleep(10 * 1000)
    if (!domain.isBackTesting) {
      start
    }
  } (scala.concurrent.ExecutionContext.Implicits.global)

}

