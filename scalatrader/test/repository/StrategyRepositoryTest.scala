package repository

import domain.Side.Buy
import domain.models.Ordering
import domain.strategy.StrategyState
import org.scalatest.FunSuite
import repository.model.scalatrader.User
import scalikejdbc.ConnectionPool

class StrategyRepositoryTest extends FunSuite {

  ignore("testSelect") {
    ConnectionPool.singleton("jdbc:mysql://localhost:6603/scalatrader", "root", "password")
    val user = User(1L, "hoge", "hoge", "hoge", "hoge", "hoge")
    println(StrategyRepository.list(user))
  }

  ignore("testInsert") {
    val state = StrategyState(0L, "MomentumReverse", false, 1.5, Some("xxx-xx-xx"), Some(Ordering(Buy, 0.2, true)), Map.empty)
    ConnectionPool.singleton("jdbc:mysql://localhost:6603/scalatrader", "root", "password")
    val user = User(1L, "hoge", "hoge", "hoge", "hoge", "hoge")
    StrategyRepository.store(user, state)
  }
  ignore("testDelete") {
    ConnectionPool.singleton("jdbc:mysql://localhost:6603/scalatrader", "root", "password")
    val user = User(1L, "hoge", "hoge", "hoge", "hoge", "hoge")
    StrategyRepository
      .list(user)
      .foreach(state => {
        StrategyRepository.delete(user, state.id)
      })
  }

}
