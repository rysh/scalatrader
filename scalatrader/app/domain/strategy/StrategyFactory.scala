package domain.strategy

import domain.strategy.box._
import domain.strategy.momentum.MomentumReverseStrategy
import domain.strategy.other.OnceForTestStrategy
import domain.strategy.sfd.SfdStrategy
import domain.strategy.turtle.TurtleStrategy
import repository.model.scalatrader.User

object StrategyFactory {
  val MomentumReverse = "MomentumReverse"
  val BoxReverseLimit = "BoxReverseLimit"
  val MixedBoxesStrategy = "MixedBoxesStrategy"
  val BoxTrendStrategy = "BoxTrendStrategy"
  val options = Map(
    MomentumReverse -> MomentumReverse,
    BoxReverseLimit -> BoxReverseLimit,
    MixedBoxesStrategy -> MixedBoxesStrategy,
    BoxTrendStrategy -> BoxTrendStrategy,
    "SFD" -> "SFD",
    "BoxTrendSFDStrategy" -> "BoxTrendWithoutSFDStrategy",
    "UpTrendStrategy" -> "UpTrendStrategy",
    "UpTrendSFDStrategy" -> "UpTrendSFDStrategy",
    "DownTrendStrategy" -> "DownTrendStrategy",
    "DownTrendSFDStrategy" -> "DownTrendSFDStrategy",
    "Turtle" -> "Turtle"
  )

  def create(state: StrategyState, user: User): Strategy = {
    state.name match {
      case MomentumReverse              => new MomentumReverseStrategy(state, user)
      case "Turtle"                     => new TurtleStrategy(state, user)
      case "BoxReverseLimit"            => new BoxReverseLimitStrategy(state, user)
      case "MixedBoxesStrategy"         => new MixedBoxesStrategy(state, user)
      case "BoxTrendStrategy"           => new BoxTrendStrategy(state, user)
      case "SFD"                        => new SfdStrategy(state, user)
      case "BoxTrendWithoutSFDStrategy" => new BoxTrendWithoutSFDStrategy(state, user)
      case "UpTrendStrategy"            => new UpTrendStrategy(state, user)
      case "UpTrendSFDStrategy"         => new UpTrendSFDStrategy(state, user)
      case "DownTrendStrategy"          => new DownTrendStrategy(state, user)
      case "DownTrendSFDStrategy"       => new DownTrendSFDStrategy(state, user)
      case "Once"                       => new OnceForTestStrategy(state, user)
      case _                            => throw new Exception()
    }
  }
}
