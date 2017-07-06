package adapter.model


object BitFlyer {
  import io.circe.generic.extras._, io.circe.syntax._
  implicit val config: Configuration = Configuration.default.withSnakeCaseKeys

  @ConfiguredJsonCodec
  case class TickerInfo(
    productCode:String,
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

}
