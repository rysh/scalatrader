package object domain {

  object ProductCode {
    val btcFx = "FX_BTC_JPY"
  }

  object Side {
    val Sell = "SELL"
    val Buy = "BUY"
    def of(relative: Double) =  if (relative < 0) Sell else Buy
  }

  var isBackTesting = false
}
