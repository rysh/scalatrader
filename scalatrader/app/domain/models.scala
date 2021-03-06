package domain

object models {
  case class Execution(
      id: Long,
      side: String,
      price: Double,
      size: Double,
      exec_date: String,
      buy_child_order_acceptance_id: String,
      sell_child_order_acceptance_id: String
  )

  case class Collateral(
      /** 証拠金の評価額 */
      collateral: Double,
      /** 建玉の評価損益 */
      open_position_pnl: Double,
      /** 必要証拠金 */
      require_collateral: Double,
      /** 証拠金維持率 */
      keep_rate: Double,
  )

  case class Position(
      product_code: String,
      side: String,
      price: Double,
      size: Double,
      commission: Double,
      swap_point_accumulate: Double,
      require_collateral: Double,
      open_date: String,
      leverage: Double,
      pnl: Double,
  ) {
    def relativeSize = size * (if (side == domain.Side.Sell) -1 else 1)
  }

  case class Positions(values: Seq[Position]) {
    def btcFx = absoluteSize(ProductCode.btcFx)
    def absoluteSize(product_code: String) = get(product_code).map(p => if (p.side == Side.Sell) -p.size else p.size).sum
    def get(product_code: String): Seq[Position] = values.filter(p => p.product_code == product_code)
  }

  case class Ticker(
      product_code: String,
      timestamp: String,
      tick_id: Long,
      best_bid: Double,
      best_ask: Double,
      best_bid_size: Double,
      best_ask_size: Double,
      total_bid_depth: Double,
      total_ask_depth: Double,
      ltp: Double,
      volume: Double,
      volume_by_product: Double,
  )

  case class Order(
      product_code: String,
      /** 指値注文の場合は "LIMIT", 成行注文の場合は "MARKET" を指定 */
      child_order_type: String,
      /** 買い注文の場合は "BUY", 売り注文の場合は "SELL" を指定 */
      side: String,
      /** 価格を指定。child_order_type に "LIMIT" を指定した場合は必須 */
      price: Option[Double],
      /** 注文数量を指定 */
      size: Double,
      /** 期限切れまでの時間を分で指定 */
      minute_to_expire: Int,
      /** 執行数量条件 を "GTC", "IOC", "FOK"のいずれかで指定 */
      time_in_force: String,
  ) {
    def relativeSize = size * (if (side == domain.Side.Sell) -1 else 1)
  }
  object Orders {
    def market(t: Ordering): Order = market(t.side, t.size)
    def market(side: String, size: Double): Order = Order(ProductCode.btcFx, "MARKET", side, None, size, 5, "GTC")
  }

  case class Ordering(side: String, size: Double, isEntry: Boolean)
}
