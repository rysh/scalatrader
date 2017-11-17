package application

import adapter.Store
import adapter.bitflyer.realtime.{PubNubReceiver, TickerCallback}
import com.amazonaws.regions.Regions

object Main extends App {

  val bucketName = "btcfx-ticker-scala"
  val code = "lightning_ticker_FX_BTC_JPY"
  val key =  "sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f"
  val region = Regions.US_WEST_1

  Validations.bucketExists(bucketName, region)
  Validations.workingDirectoryExisits

  PubNubReceiver.start(code, key, new TickerCallback(Store.s3(bucketName, region)))

}

