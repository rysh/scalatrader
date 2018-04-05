package application

import java.time.ZonedDateTime

import javax.inject.Named
import adapter.BitFlyer
import adapter.aws.{MailContent, SES, OrderQueueBody, SQS}
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
import domain.time.DateUtil
import play.api.{Configuration, Logger}
import service.DataLoader

import scala.concurrent.Future

@Singleton
class RealTimeReceiver @Inject()(config: Configuration, @Named("candle") candleActor: ActorRef, strategySettingApplication: StrategySettingApplication, sqs: SQS) {
  Logger.info("init RealTimeReceiver")

  def start(): Unit = {
    val gson: Gson = new Gson()

    val productCode = s"lightning_ticker_${ProductCode.btcFx}"
    val key = "sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f"
    val callback = new SubscribeCallback() {
      override def message(pubnub: PubNub, message: PNMessageResult): Unit = {
        val ticker: Ticker = gson.fromJson(message.getMessage, classOf[Ticker])
        val recentTickerTime = ZonedDateTime.parse(ticker.timestamp).minusSeconds(1)
        Strategies.values.filter(_.isAvailable) foreach (strategy => {
          Future { // parallel for each strategy
            strategy.synchronized { // run in order　in the same strategy
              if (recentTickerTime.isAfter(ZonedDateTime.parse(ticker.timestamp))) { // skip old ticker after retrying
                (try {
                  strategy.judgeByTicker(ticker)
                } catch {
                  case e: Exception =>
                    e.printStackTrace()
                    None
                }).foreach(ordering => {
                  val now = DateUtil.now().toString
                  val order: models.Order = Orders.market(ordering)
                  Logger.info(
                    s"[order][${strategy.state.id}][${if (ordering.isEntry) "entry" else "close"}:${order.side}][${ticker.timestamp}] price:${ticker.ltp.toLong} size:${order.size}")
                  (try {
                    Some(retry(if (ordering.isEntry) 5 else 20, () => BitFlyer.orderByMarket(order, strategy.key, strategy.secret)))
                  } catch {
                    case _: Exception =>
                      // request error case
                      strategy.state.orderId = None
                      strategy.state.order = None
                      if (!ordering.isEntry) {
                        Logger.warn("!!!close request failed.!!!")
                        sendRequestFailureNoticeMail(strategy, ordering)
                      }
                      None
                  }).foreach(response => {
                    val newState = if (ordering.isEntry) {
                      // entry case
                      sqs.send(OrderQueueBody(strategy.email, strategy.state.id, response.child_order_acceptance_id, now))
                      strategy.state.copy(orderId = Some(response.child_order_acceptance_id), order = Some(ordering))
                    } else {
                      // close case
                      sqs.send(OrderQueueBody(strategy.email, strategy.state.id, response.child_order_acceptance_id, now, strategy.state.orderId))
                      strategy.state.copy(orderId = None, order = None)
                    }
                    strategySettingApplication.updateOrder(strategy.email, newState)
                  })
                })
              }
            }
          }(scala.concurrent.ExecutionContext.Implicits.global)
        })
        Strategies.putTicker(ticker)
      }
      override def presence(pubnub: PubNub, presence: PNPresenceEventResult): Unit = {
        Logger.info("RealTimeReceiver#presence")
        Logger.info(presence.toString)
      }

      override def status(pubnub: PubNub, status: PNStatus): Unit = {
        Logger.info("RealTimeReceiver#status")
        Logger.info(status.toString)
      }
    }

    PubNubReceiver.start(productCode, key, callback)
    Logger.info("PubNubReceiver started")
    def loadInitialData() = {
      Future {
        val initialData: Seq[Ticker] = DataLoader.loadFromS3()
//        val initialData: Seq[Ticker] = DataLoader.loadFromLocal()
        initialData.foreach(ticker => {
          Strategies.coreData.putTicker(ticker)
          Strategies.values.foreach(_.putTicker(ticker))
        })
        Strategies.processEvery1minutes()
        DataLoader.loaded = true
      }(scala.concurrent.ExecutionContext.Implicits.global)
    }
    loadInitialData()
  }

  private def sendRequestFailureNoticeMail(strategy: Strategy, ordering: models.Ordering): Unit = {
    val subject = "close request failed"
    val text = s"failed: ${ordering.side} size:${ordering.size}"
    SES.send(MailContent(strategy.email, "info@scalatrader.com", subject, text, text))
  }

  Future {
    Thread.sleep(10 * 1000)
    if (!domain.isBackTesting) {
      start
    }
  }(scala.concurrent.ExecutionContext.Implicits.global)

}
