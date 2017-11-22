package adapter.bitflyer

object Path {
  val BASE = "https://api.bitflyer.jp"
  val EXECUTIONS = "/v1/executions"
  val COLLATERAL = "/v1/me/getcollateral"
  val POSITIONS = "/v1/me/getpositions?product_code=FX_BTC_JPY"
}
