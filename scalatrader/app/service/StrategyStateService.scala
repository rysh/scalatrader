package service

import scala.concurrent.ExecutionContext
import javax.inject.Inject

import adapter.BitFlyer
import domain.models.Orders
import domain.strategy.StrategyState
import play.api.{Configuration, Logger}
import repository.model.scalatrader.User

class StrategyStateService @Inject()(config: Configuration)(implicit executionContext: ExecutionContext) {

  def reverseOrder(user: User, state: StrategyState):Unit = {
    val side = domain.reverseSide(state.order.get.side)
    val size = state.order.get.size
    reverseOrder(user, side, size)
  }

  def reverseOrder(user: User, side: String, size: Double):Unit = {
    if (!domain.isBackTesting) {
      val order = Orders.market(side, size)
      BitFlyer.orderByMarket(order, user.api_key, user.api_secret)
      Logger.info(s"reverse ordered : $side size:$size (${user.email})")
    } else {
      Logger.info(s"deleted: $side size:$size")
    }
  }
}
