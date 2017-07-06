package artifact

import adapter.bitflyer.model.BitFlyer._
import org.scalatest._

class JsonParserSpec extends FlatSpec with Matchers {
  val input =
    """{"product_code":"FX_BTC_JPY","timestamp":"2017-07-04T09:50:28.9864935Z","tick_id":3124876,"best_bid":296269.0,"best_ask":296411.0,"best_bid_size":1.34,"best_ask_size":0.54,"total_bid_depth":9256.98508692,"total_ask_depth":6999.18986209,"ltp":296267.0,"volume":78373.73225348,"volume_by_product":78373.73225348}""".stripMargin

  def create():TickerInfo = {
    TickerInfo(productCode = "FX_BTC_JPY",
      timestamp = "2017-07-04T09:50:28.9864935Z",
      tickId= 3124876,
      bestBid = 296269.0,
      bestAsk = 296411.0,
      bestBidSize = 1.34,
      bestAskSize = 0.54,
      totalBidDepth = 9256.98508692,
      totalAskDepth = 6999.18986209,
      ltp = 296267.0,
      volume = 78373.73225348,
      volume_by_product = 78373.73225348)
  }

  it should "encode to JSON" in {
    val ticker = create()

    ticker.bestAsk should be(296411.0)

    import io.circe.syntax._
    ticker.asJson.noSpaces should be (input)
  }



  it should "parse JSON" in {

    import io.circe.generic.auto._
    import io.circe.parser._

    decode[TickerInfo](input).getOrElse(Nil) should be (create())
  }

}
