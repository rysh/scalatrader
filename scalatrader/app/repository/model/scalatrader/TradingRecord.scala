package repository.model.scalatrader

import java.time.ZonedDateTime

import adapter.BitFlyer.MyExecution

case class TradingRecord2(id: Long,
                          email: String,
                          tradingRuleStateId: Long,
                          entryId: String,
                          entryExecution: Seq[MyExecution],
                          entryTimestamp: ZonedDateTime,
                          closeId: Option[String] = None,
                          closeExecution: Seq[MyExecution],
                          closeTimestamp: Option[ZonedDateTime] = None,
                          timestamp: ZonedDateTime)
