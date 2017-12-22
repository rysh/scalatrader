package application

import javax.inject.Inject

import adapter.BitFlyer
import domain.models.Orders
import domain.strategy.{StrategyState, StrategyFactory, Strategies}
import play.api.{Configuration, Logger}
import repository.{StrategyRepository, UserRepository}
import repository.model.scalatrader.User

import scala.concurrent.ExecutionContext

class InitializeService @Inject()(config: Configuration)(implicit executionContext: ExecutionContext) {
  Logger.info("InitializeService load")

  val secret: String = config.get[String]("play.http.secret.key")

  def reverseOrder(user: User, side: String, size: Double):Unit = {
    val order = Orders.market(side, size)
    BitFlyer.orderByMarket(order, user.api_key, user.api_secret)
    Logger.info(s"reverse ordered (${user.email})")
  }

  def restoreStrategies(): Unit = {
    UserRepository.all(secret).foreach(user => {
      StrategyRepository.list(user).filter(_.availability).foreach((state: StrategyState) => {
        Strategies.register(StrategyFactory.create(state, user))
      })
    })
  }
}