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
  )

  case class Positions(values: Seq[Position]) {
    def delta = values.map(p => if (p.side == "SELL") (-p.size) else p.size).sum
  }

}
