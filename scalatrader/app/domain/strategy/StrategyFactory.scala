package domain.strategy

import domain.strategy.box.BoxReverseLimitStrategy
import domain.strategy.momentum.MomentumReverseStrategy
import domain.strategy.turtle.TurtleStrategy
import repository.model.scalatrader.User

object StrategyFactory {
  val MomentumReverse = "MomentumReverse"
  val BoxReverseLimit = "BoxReverseLimit"
  val options = Map(MomentumReverse -> MomentumReverse, BoxReverseLimit -> BoxReverseLimit, "Turtle" -> "Turtle")

  def create(state: StrategyState, user:User): Strategy = {
    state.name match {
      case MomentumReverse => new MomentumReverseStrategy(state, user)
      case "Turtle" => new TurtleStrategy(state, user)
      case "BoxReverseLimit" => new BoxReverseLimitStrategy(state, user)
      case _ => throw new Exception()
    }
  }
}
