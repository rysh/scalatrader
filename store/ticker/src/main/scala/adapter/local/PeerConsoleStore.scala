package adapter.local

import adapter.Store
import com.google.gson.JsonElement

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
object PeerConsoleStore extends Store {
  override def store(json: JsonElement): Unit = {
    println(json)
  }

  override def write(): Either[Unit, Store] = Left()
}
