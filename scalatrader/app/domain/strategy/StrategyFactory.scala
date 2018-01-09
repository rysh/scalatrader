package domain.strategy

import domain.strategy.StrategyFactory.BoxTrendStrategy
import domain.strategy.box.{BoxReverseLimitStrategy, MixedBoxesStrategy, BoxTrendStrategy}
import domain.strategy.momentum.MomentumReverseStrategy
import domain.strategy.turtle.TurtleStrategy
import repository.model.scalatrader.User

object StrategyFactory {
  val MomentumReverse = "MomentumReverse"
  val BoxReverseLimit = "BoxReverseLimit"
  val MixedBoxesStrategy = "MixedBoxesStrategy"
  val BoxTrendStrategy = "BoxTrendStrategy"
  val options = Map(MomentumReverse -> MomentumReverse,
    BoxReverseLimit -> BoxReverseLimit,
    MixedBoxesStrategy -> MixedBoxesStrategy,
    BoxTrendStrategy -> BoxTrendStrategy,
    "Turtle" -> "Turtle")

  def create(state: StrategyState, user:User): Strategy = {
    state.name match {
      case MomentumReverse => new MomentumReverseStrategy(state, user)
      case "Turtle" => new TurtleStrategy(state, user)
      case "BoxReverseLimit" => new BoxReverseLimitStrategy(state, user)
      case "MixedBoxesStrategy" => new MixedBoxesStrategy(state, user)
      case "BoxTrendStrategy" => new BoxTrendStrategy(state, user)
      case _ => throw new Exception()
    }
  }
}
