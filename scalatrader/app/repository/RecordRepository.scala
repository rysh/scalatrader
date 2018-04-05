package repository

import java.time.ZonedDateTime

import adapter.BitFlyer
import adapter.BitFlyer.MyExecution
import domain.strategy.StrategyState
import io.circe
import models.TradingRecord
import models.TradingRecord.tr
import repository.model.scalatrader.TradingRecord2
import scalikejdbc.{AutoSession, _}

object RecordRepository {
  val tr = TradingRecord.syntax("tr")

  def insert(email: String, strategyStateId: Long, child_order_acceptance_id: String, entry: Seq[MyExecution], time: ZonedDateTime): Unit = {
    implicit val session = AutoSession
    import io.circe.syntax._
    import io.circe.generic.auto._
    val json = entry.asJson.toString()
    sql"insert into trading_record (email, trading_rule_state_id, entry_id, entry_execution, entry_timestamp) values ($email, $strategyStateId, $child_order_acceptance_id, $json, $time)".update
      .apply()
  }

  def update(email: String, child_order_acceptance_id: String, entry_id: String, close: Seq[MyExecution], time: ZonedDateTime): Int = {
    implicit val session = AutoSession
    import io.circe.syntax._
    import io.circe.generic.auto._
    val json = close.asJson.toString()
    sql"update trading_record set close_id = $child_order_acceptance_id, close_execution =  $json, close_timestamp = $time where email = $email and entry_id = $entry_id".update
      .apply()
  }

  def findAll(email: String, strategyStateId: Long, from: ZonedDateTime): List[TradingRecord2] = {
    implicit val session = AutoSession
    val query = sql"select * from trading_record where email = ${email} and trading_rule_state_id = ${strategyStateId} and entry_timestamp > ${from}"
    query
      .map((rs: WrappedResultSet) => {
        val jsonToExecutions = (json: String) => {
          import io.circe.parser._
          import io.circe.generic.auto._
          decode[Seq[MyExecution]](json).fold(_ => None, Some(_)).getOrElse(Seq.empty[MyExecution])
        }
        TradingRecord2(
          rs.long("id"),
          rs.string("email"),
          rs.long("trading_rule_state_id"),
          rs.string("entry_id"),
          jsonToExecutions(rs.string("entry_execution")),
          rs.zonedDateTime("entry_timestamp"),
          Option(rs.string("close_id")),
          jsonToExecutions(rs.string("close_execution")),
          Option(rs.zonedDateTime("close_timestamp")),
          rs.zonedDateTime("timestamp")
        )
      })
      .list
      .apply()

  }
}
