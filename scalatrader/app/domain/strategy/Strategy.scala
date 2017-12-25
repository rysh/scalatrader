package domain.strategy

import domain.margin.Margin
import domain.models
import domain.models.{Ticker, Ordering}
import repository.model.scalatrader.User

abstract class Strategy(
  var state: StrategyState,
  user: User) {

  val email: String = user.email
  val key: String = user.api_key
  val secret: String = user.api_secret

  // state for system
  val availability = new Availability(state)
  def isAvailable: Boolean = availability.isAvailable

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
    availability.state = newState
    state = newState
  }

}

class Availability(var state: StrategyState) {
  def manualOn: Boolean = state.availability
  var initialDataLoaded = false
  def isAvailable: Boolean = manualOn && initialDataLoaded
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