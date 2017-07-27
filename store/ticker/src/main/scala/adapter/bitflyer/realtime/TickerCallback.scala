package adapter.bitflyer.realtime

import adapter.Store
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.{PNMessageResult, PNPresenceEventResult}

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
class TickerCallback(var store: Store) extends SubscribeCallback {

  override def message(pubnub: PubNub, message: PNMessageResult): Unit = {
    store.write() match {
      case Right(s) => store = s
      case Left(_) => {}
    }

    val json = message.getMessage
    store.store(json)
  }


  override def presence(pubnub: PubNub, presence: PNPresenceEventResult): Unit = {
    println(presence)
  }

  override def status(pubnub: PubNub, status: PNStatus): Unit = {
    println(status)
  }
}
