package application

import adapter.Store
import adapter.bitflyer.realtime.{PubNubReceiver, TickerCallback}
import com.amazonaws.regions.Regions

object Main extends App {

  val key =  "sub-c-52a9ab50-291b-11e5-baaa-0619f8945a4f"
  val region = Regions.US_WEST_1

  private def exec(bucketName: String, productCode: String, region: Regions, key: String) = {
    Validations.bucketExists(bucketName, region)
    Validations.workingDirectoryExisits(bucketName)
    PubNubReceiver.start(productCode, key, new TickerCallback(Store.s3(bucketName, region)))
  }

  exec("btcfx-ticker-scala", "lightning_ticker_FX_BTC_JPY", region, key)
  exec("btcfx-executions-scala", "lightning_executions_FX_BTC_JPY", region, key)

}

