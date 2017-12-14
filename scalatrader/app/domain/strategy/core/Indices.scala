package domain.strategy.core


import domain.strategy.Strategies

object Indices {


  def isUpTrend: Option[Boolean] = {
    import Strategies._
    for {
      box10m <- coreData.box10min
      box20m <- coreData.box20min
    } yield {
      box20m.high == box10m.high && box20m.low < box10m.low
    }
  }

  def isDownTrend: Option[Boolean] = {
    import Strategies._
    for {
      box10m <- coreData.box10min
      box20m <- coreData.box20min
    } yield {
      box20m.low == box10m.low && box20m.high > box10m.high
    }
  }
}
