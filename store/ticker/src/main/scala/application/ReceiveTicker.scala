package application

import adapter.bitflyer.realtime.{PubNubReceiver, StoreS3, TickerCallback}
import com.google.gson.JsonElement

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
object ReceiveTicker {

  def start(code: String, key: String) = {
    PubNubReceiver.start(code, key, new TickerCallback(new StoreS3))
  }
}
