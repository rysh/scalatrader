package adapter.bitflyer.realtime

import com.google.gson.JsonElement

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
object PeerConsole extends Store {
  override def store(json: JsonElement): Unit = {
    println(json)
  }
}
