package adapter.bitflyer

import domain.ProductCode

object Path {
  val BASE = "https://api.bitflyer.jp"
  val EXECUTIONS = "/v1/executions"
  val COLLATERAL = "/v1/me/getcollateral"
  val POSITIONS = s"/v1/me/getpositions?product_code=${ProductCode.btcFx}"
  val CHILD_ORDER = "/v1/me/sendchildorder"
  val ME_EXECUTIONS = "/v1/me/getexecutions"
}
