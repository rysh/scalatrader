package adapter.bitflyer.model


object BitFlyer {

  case class TickerInfo(
    productCode:String,
    timestamp:String,
    tickId:Int,
    bestBid:Double,
    bestAsk:Double,
    bestBidSize:Double,
    bestAskSize:Double,
    totalBidDepth: Double,
    totalAskDepth:Double,
    ltp:Double,
    volume:Double,
    volume_by_product:Double)

}
