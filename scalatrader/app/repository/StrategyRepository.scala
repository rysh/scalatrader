package repository

import domain.strategy.StrategyState
import domain.util.crypto.Aes
import repository.UserRepository.map
import repository.model.scalatrader.{User, TradingRuleState}
import scalikejdbc.AutoSession
import scalikejdbc._

object StrategyRepository {

  def store(user: User, state: StrategyState): Unit = {
    import io.circe.syntax._
    import io.circe.generic.auto._
    val json = state.asJson.toString()

    implicit val session = AutoSession
    sql"insert into trading_rule_state (user_id, trading_rule_name, state) values (${user.id}, ${state.name}, $json)".update.apply()
  }

  def list(user: User): Seq[StrategyState] = {
    implicit val session = AutoSession
    sql"select id, state from trading_rule_state where user_id = ${user.id}"
      .map((rs: WrappedResultSet) => {
        import io.circe.parser._
        import io.circe.generic.auto._
        decode[StrategyState](rs.string("state")) match {
          case Right(ex) =>
            ex.id = rs.long("id")
            ex
          case Left(err) => throw err
        }
      })
      .list()
      .apply()
  }

  def get(user: User, id: Long): Option[StrategyState] = {
    implicit val session = AutoSession
    sql"select id, state from trading_rule_state where user_id = ${user.id} and id =$id"
      .map((rs: WrappedResultSet) => {
        import io.circe.parser._
        import io.circe.generic.auto._
        decode[StrategyState](rs.string("state")) match {
          case Right(ex) =>
            ex.id = rs.long("id")
            ex
          case Left(err) => throw err
        }
      })
      .single()
      .apply()
  }

  def update(user: User, state: StrategyState): Unit = {
    import io.circe.syntax._
    import io.circe.generic.auto._
    val json = state.asJson.toString()

    implicit val session = AutoSession
    sql"update trading_rule_state set state = $json where user_id = ${user.id} and id = ${state.id}".update.apply()
  }

  def delete(user: User, id: Long): Int = {
    implicit val session = AutoSession
    sql"delete from trading_rule_state where user_id = ${user.id} and id = ${id}".update.apply()
  }

}
