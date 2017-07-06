import collection.mutable.Stack
import org.scalatest._


class JsonParserSpec extends FlatSpec with Matchers {
  val input =
    """{"product_code":"FX_BTC_JPY","timestamp":"2017-07-04T09:50:28.9864935Z","tick_id":3124876,"best_bid":296269.0,"best_ask":296411.0,"best_bid_size":1.34,"best_ask_size":0.54,"total_bid_depth":9256.98508692,"total_ask_depth":6999.18986209,"ltp":296267.0,"volume":78373.73225348,"volume_by_product":78373.73225348}""".stripMargin
  import io.circe.generic.extras._, io.circe.syntax._
  implicit val config: Configuration = Configuration.default.withSnakeCaseKeys

  @ConfiguredJsonCodec case class TickerInfo(productCode:String,
                                             timestamp:String,
                                             tickId:Int,
                                             bestBid:BigDecimal,
                                             bestAsk:BigDecimal,
                                             bestBidSize:BigDecimal,
                                             bestAskSize:BigDecimal,
                                             totalBidDepth: BigDecimal,
                                             totalAskDepth:BigDecimal,
                                             ltp:BigDecimal,
                                             volume:BigDecimal,
                                             volume_by_product:BigDecimal)


  it should "JSONをパースできる" in {
    val bestAsk = 296411.0

    val ticker = TickerInfo(productCode = "FX_BTC_JPY",
      timestamp = "2017-07-04T09:50:28.9864935Z",
      tickId= 3124876,
      bestBid = 296269.0,
      bestAsk = bestAsk,
      bestBidSize = 1.34,
      bestAskSize = 0.54,
      totalBidDepth = 9256.98508692,
      totalAskDepth = 6999.18986209,
      ltp = 296267.0,
      volume = 78373.73225348,
      volume_by_product = 78373.73225348)

    ticker.bestAsk should be (bestAsk)
    ticker.asJson.noSpaces should be (input)
  }

}
