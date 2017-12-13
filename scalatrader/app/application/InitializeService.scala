package application

import javax.inject.Inject

import adapter.BitFlyer
import domain.models.Orders
import domain.strategy.Strategies
import play.api.Configuration
import repository.UserRepository

import scala.concurrent.{Future, ExecutionContext}

class InitializeService @Inject()(config: Configuration)(implicit executionContext: ExecutionContext) {
  println("InitializeService load")

  val secret: String = config.get[String]("play.http.secret.key")

  def clearOldOrders(): Unit = {
    UserRepository.fetchCurrentOrder().foreach(currentOrder => {
      println(currentOrder)
      UserRepository.get(currentOrder.email, secret).foreach(user => {
        println(user)
        val order = Orders.market(currentOrder.reverseSide, currentOrder.size)
        BitFlyer.orderByMarket(order, user.api_key, user.api_secret)
        println("reverse orderd")
        UserRepository.clearCurrentOrder(user.email, currentOrder.child_order_acceptance_id)
        println("deleted")
      })
    })
  }

  Future {
    clearOldOrders()
    Strategies.values.foreach(s => s.availability.manualOn = true)
  } (scala.concurrent.ExecutionContext.Implicits.global)
}