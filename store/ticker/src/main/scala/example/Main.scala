package example

object Main extends Greeting with App {
  println(greeting)

  import com.pubnub.api.PNConfiguration
  import com.pubnub.api.PubNub
  import com.pubnub.api.callbacks.PNCallback
  import com.pubnub.api.callbacks.SubscribeCallback
  import com.pubnub.api.enums.PNStatusCategory
  import com.pubnub.api.models.consumer.PNPublishResult
  import com.pubnub.api.models.consumer.PNStatus
  import com.pubnub.api.models.consumer.pubsub.PNMessageResult
  import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
  import java.util

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

trait Greeting {
  lazy val greeting: String = "hello"
}
