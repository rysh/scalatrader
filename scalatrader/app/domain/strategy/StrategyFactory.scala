package domain.strategy

import domain.strategy.box._
import domain.strategy.dtn.{Dtn5mStrategy, Dtn1mStrategy, Dtn10mStrategy}
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
    "BoxTrendWithoutSFDStrategy" -> "BoxTrendWithoutSFDStrategy",
    "UpTrendStrategy" -> "UpTrendStrategy",
    "UpTrend2hStrategy" -> "UpTrend2hStrategy",
    "UpTrend4hStrategy" -> "UpTrend4hStrategy",
    "UpTrendSFDStrategy" -> "UpTrendSFDStrategy",
    "DownTrendStrategy" -> "DownTrendStrategy",
    "DownTrend2hStrategy" -> "DownTrend2hStrategy",
    "DownTrend4hStrategy" -> "DownTrend4hStrategy",
    "DownTrendSFDStrategy" -> "DownTrendSFDStrategy",
    "Dtn1mStrategy" -> "Dtn1mStrategy",
    "Dtn5mStrategy" -> "Dtn5mStrategy",
    "Dtn10mStrategy" -> "Dtn10mStrategy",
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
      case "UpTrend2hStrategy"          => new UpTrend2hStrategy(state, user)
      case "UpTrend4hStrategy"          => new UpTrend4hStrategy(state, user)
      case "UpTrendSFDStrategy"         => new UpTrendSFDStrategy(state, user)
      case "DownTrendStrategy"          => new DownTrendStrategy(state, user)
      case "DownTrend2hStrategy"        => new DownTrend2hStrategy(state, user)
      case "DownTrend4hStrategy"        => new DownTrend4hStrategy(state, user)
      case "DownTrendSFDStrategy"       => new DownTrendSFDStrategy(state, user)
      case "Dtn1mStrategy"              => new Dtn1mStrategy(state, user)
      case "Dtn5mStrategy"              => new Dtn5mStrategy(state, user)
      case "Dtn10mStrategy"             => new Dtn10mStrategy(state, user)
      case "Once"                       => new OnceForTestStrategy(state, user)
      case _                            => throw new Exception()
    }
  }
}
