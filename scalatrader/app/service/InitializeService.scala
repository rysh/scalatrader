package service

import javax.inject.Inject

import domain.strategy.{StrategyState, Strategies, StrategyFactory}
import play.api.{Configuration, Logger}
import repository.{StrategyRepository, UserRepository}

import scala.concurrent.{Future, ExecutionContext}

class InitializeService @Inject()(config: Configuration)(implicit executionContext: ExecutionContext) {
  Logger.info("InitializeService load")

  val secret: String = config.get[String]("play.http.secret.key")

  def restoreStrategies(): Unit = {
    UserRepository.all(secret).foreach(user => {
      StrategyRepository.list(user).filter(_.availability).foreach((state: StrategyState) => {
        Strategies.register(StrategyFactory.create(state, user))
      })
    })
  }

  Future {
    Thread.sleep(10 * 1000)
    restoreStrategies()
  } (scala.concurrent.ExecutionContext.Implicits.global)
}