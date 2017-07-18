package application

import adapter.bitflyer.realtime.{PubNubReceiver, StoreS3, TickerCallback}

object Main extends App {

  val bucketName = "btcfx-ticker-scala"
  val code = "lightning_ticker_FX_BTC_JPY"
  val key =  "sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f"

  Validations.bucketExists(bucketName)

  PubNubReceiver.start(code, key, new TickerCallback(new StoreS3(bucketName)))
  PubNubReceiver.start(code, key, new TickerCallback(new StoreS3(bucketName)))

}

