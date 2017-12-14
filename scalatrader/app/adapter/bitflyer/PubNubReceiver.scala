package adapter.bitflyer

import com.pubnub.api.PNConfiguration
import com.pubnub.api.callbacks.SubscribeCallback

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
object PubNubReceiver {

  def start(productCode: String, key: String, callback: SubscribeCallback) = {
    import java.util
    import com.pubnub.api.PubNub

    val pubnub: PubNub = new PubNub(configure(key))

    pubnub.addListener(callback)
    pubnub.subscribe.channels(util.Arrays.asList(productCode)).execute()

  }

  private def configure(key: String) :PNConfiguration = {
    val pnConfiguration = new PNConfiguration
    pnConfiguration.setSubscribeKey(key)
    pnConfiguration
  }
}
