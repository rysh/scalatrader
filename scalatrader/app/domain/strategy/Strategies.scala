package domain.strategy

import domain.models
import domain.strategy.core.CoreData

object Strategies {

  val coreData = new CoreData
  val values = collection.mutable.ArrayBuffer.empty[Strategy]

  def register(strategy: Strategy) = values.append(strategy)

  def putTicker(ticker: models.Ticker): Unit = {
    coreData.putTicker(ticker)
    values.foreach(_.putTicker(ticker))
  }

  def processEvery1minutes() = {
    values.foreach(_.processEvery1minutes)
    coreData.processEvery1minutes
  }
  def init() = {
    values.foreach(_.init())
    coreData.init()
  }

}
