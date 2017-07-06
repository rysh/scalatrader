package application

import adapter.bitflyer.realtime.TickerReceiver

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
object ReceiveTicker {

  def start() = {
    TickerReceiver.start()
  }
}
