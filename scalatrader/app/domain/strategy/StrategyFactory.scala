package domain.strategy

import domain.strategy.momentum.MomentumReverseStrategy
import domain.strategy.turtle.TurtleStrategy
import repository.model.scalatrader.User

object StrategyFactory {
  val MomentumReverse = "MomentumReverse"
  val options = Map(MomentumReverse -> MomentumReverse, "Turtle" -> "Turtle")

  def create(state: StrategyState, user:User): Strategy = {
    state.name match {
      case MomentumReverse => new MomentumReverseStrategy(state, user)
      case "Turtle" => new TurtleStrategy(state, user)
      case _ => throw new Exception()
    }
  }
}
