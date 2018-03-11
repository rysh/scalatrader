package repository.model.scalatrader

import adapter.BitFlyer.MyExecution

case class User(id: Long, email: String, password: String, name: String, api_key: String, api_secret: String)

case class TradingRecord(
    id: Long,
    email: String,
    entry_id: String,
    entry_execution: Option[MyExecution],
    entry_timestamp: String,
    close_id: Option[String],
    close_execution: Option[MyExecution],
    close_timestamp: Option[String],
    timestamp: String
)
