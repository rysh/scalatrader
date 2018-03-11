package repository.model.scalatrader

case class TradingRuleState(
    id: Long,
    user_id: Long,
    trading_rule_name: String,
    variety: String,
    state: String,
)
