package application

import javax.inject.Inject

import adapter.BitFlyer
import domain.models.Orders
import domain.strategy.Strategies
import play.api.{Configuration, Logger}
import repository.UserRepository

import scala.concurrent.{Future, ExecutionContext}

class InitializeService @Inject()(config: Configuration)(implicit executionContext: ExecutionContext) {
  Logger.info("InitializeService load")

  val secret: String = config.get[String]("play.http.secret.key")

  def clearOldOrders(): Unit = {
    UserRepository.fetchCurrentOrder().foreach(currentOrder => {
      Logger.info(currentOrder.toString)
      UserRepository.get(currentOrder.email, secret).foreach(user => {
        Logger.info(user.toString)
        val order = Orders.market(currentOrder.reverseSide, currentOrder.size)
        BitFlyer.orderByMarket(order, user.api_key, user.api_secret)
        Logger.info("reverse orderd")
        UserRepository.clearCurrentOrder(user.email, currentOrder.child_order_acceptance_id)
        Logger.info("deleted")
      })
    })
  }

  Future {
    clearOldOrders()
    Strategies.values.foreach(s => s.availability.manualOn = true)
  } (scala.concurrent.ExecutionContext.Implicits.global)
}