package domain.strategy

import domain.margin.Margin
import domain.models
import domain.models.{Ticker, Ordering}
import repository.model.scalatrader.User
import service.DataLoader

abstract class Strategy(
  var state: StrategyState,
  user: User) {

  val email: String = user.email
  val key: String = user.api_key
  val secret: String = user.api_secret

  // state for system
  def isAvailable: Boolean = state.availability && DataLoader.loaded

  // main logic
  def judgeByTicker(ticker: Ticker): Option[Ordering] = None
  def judgeEveryMinutes(key: Long): Option[Ordering] = None


  // maintenance
  def init():Unit = {
    state.order = None
  }
  def putTicker(ticker: models.Ticker): Unit = {}
  def processEvery1minutes():Unit = ()

  // operation
  private val entry: Boolean  = true
  def entry(size: String): Option[Ordering] = {
    state.order = Some(Ordering(size, Margin.size(state.leverage), entry))
    state.order
  }
  def close(): Option[Ordering] = {
    val side = domain.reverseSide(state.order.get.side)
    val size = state.order.map(_.size).getOrElse(Margin.size(state.leverage))
    state.order = None
    Some(Ordering(side, size, !entry))
  }
  def update(newState: StrategyState): Unit = {
    state = newState
  }

}


case class StrategyState(
  var id: Long,
  name: String,
  availability: Boolean,
  leverage: Double,
  var orderId: Option[String] = None,
  var order: Option[Ordering] = None,
  params: Map[String, String] = Map.empty,
)