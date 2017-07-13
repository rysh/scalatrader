package adapter.bitflyer.realtime

import com.google.gson.JsonElement

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
trait Store {
  def keep(json : JsonElement) : Unit
  def store(): Unit
}
