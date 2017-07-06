package adapter.bitflyer.realtime

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
object TickerReceiver {

  def start() = {
    import java.util

    import com.pubnub.api.{PNConfiguration, PubNub}
    import com.pubnub.api.callbacks.SubscribeCallback
    import com.pubnub.api.models.consumer.PNStatus
    import com.pubnub.api.models.consumer.pubsub.{PNMessageResult, PNPresenceEventResult}

    val pnConfiguration = new PNConfiguration
    pnConfiguration.setSubscribeKey("sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f")

    val pubnub = new PubNub(pnConfiguration)

    pubnub.addListener(new SubscribeCallback() {
      override def status(pubnub: PubNub, status: PNStatus): Unit = {
        println(status)
      }

      override def message(pubnub: PubNub, message: PNMessageResult): Unit = {
        println(message.getMessage)
      }

      override def presence(pubnub: PubNub, presence: PNPresenceEventResult): Unit = {
      }
    })

    pubnub.subscribe.channels(util.Arrays.asList("lightning_ticker_FX_BTC_JPY")).execute()
  }
}
