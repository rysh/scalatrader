package adapter.bitflyer.realtime

import com.google.gson.JsonElement

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
trait Store {
  def store(json : JsonElement) : Unit
}
