package application

import javax.inject.Named

import adapter.bitflyer.PubNubReceiver
import akka.actor.ActorRef
import com.google.gson.Gson
import com.google.inject.{Inject, Singleton}
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.{PNPresenceEventResult, PNMessageResult}
import domain.ProductCode
import domain.models.Ticker
import domain.strategy.Strategies
import play.api.{Configuration, Logger}

@Singleton
class BtcTickerReceiver @Inject()(config: Configuration, @Named("candle") candleActor: ActorRef, strategySettingApplication: StrategySettingApplication) {
  Logger.info("init BtcTickerReceiver")

  def start(): Unit = {
    val gson: Gson = new Gson()

    val productCode = s"lightning_ticker_${ProductCode.BTC}"
    val key = "sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f"
    val callback = new SubscribeCallback() {
      override def message(pubnub: PubNub, message: PNMessageResult): Unit = {
        val ticker: Ticker = gson.fromJson(message.getMessage, classOf[Ticker])
        Strategies.putBtcTicker(ticker)
      }
      override def presence(pubnub: PubNub, presence: PNPresenceEventResult): Unit = {
        Logger.info("BtcTickerReceiver#presence")
        Logger.info(presence.toString)
      }

      override def status(pubnub: PubNub, status: PNStatus): Unit = {
        Logger.info("BtcTickerReceiver#status")
        Logger.info(status.toString)
      }
    }

    PubNubReceiver.start(productCode, key, callback)
    Logger.info("BtcTickerReceiver started")
  }
  start()
}
