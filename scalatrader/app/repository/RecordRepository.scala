package repository

import java.time.ZonedDateTime

import adapter.BitFlyer.MyExecution
import scalikejdbc.{AutoSession, _}

object RecordRepository {

  def insert(email:String, strategyStateId: Long, child_order_acceptance_id: String, entry: Seq[MyExecution], time: ZonedDateTime): Unit = {
    implicit val session = AutoSession
    import io.circe.syntax._
    import io.circe.generic.auto._
    val json = entry.asJson.toString()
    sql"insert into trading_record (email, trading_rule_state_id, entry_id, entry_execution, entry_timestamp) values ($email, $strategyStateId, $child_order_acceptance_id, $json, $time)".update.apply()
  }

  def update(email:String, child_order_acceptance_id: String, entry_id: String, close: Seq[MyExecution], time: ZonedDateTime): Int ={
    implicit val session = AutoSession
    import io.circe.syntax._
    import io.circe.generic.auto._
    val json = close.asJson.toString()
    sql"update trading_record set close_id = $child_order_acceptance_id, close_execution =  $json, close_timestamp = $time where email = $email and entry_id = $entry_id".update.apply()
  }
}