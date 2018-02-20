package domain.strategy

import domain.models
import domain.strategy.core.CoreData
import repository.model.scalatrader.User

import scala.collection.mutable.ArrayBuffer

object Strategies {

  val coreData = new CoreData
  val values: ArrayBuffer[Strategy] = collection.mutable.ArrayBuffer.empty[Strategy]

  def register(strategy: Strategy): Unit = values.append(strategy)

  def putTicker(ticker: models.Ticker): Unit = {
    coreData.putTicker(ticker)
    values.foreach(_.putTicker(ticker))
  }

  def processEvery1minutes(): Unit = {
    values.foreach(_.processEvery1minutes())
    coreData.processEvery1minutes()
  }

  def init(): Unit = {
    values.foreach(_.init())
    coreData.init()
  }

  def update(newState: StrategyState): Unit = {
    values.find(_.state.id == newState.id).foreach(strategy => strategy.update(newState))
  }

  def remove(user: User, id: Long): Unit = {
    values.toSeq.filter(st => st.state.id == id && st.email == user.email)
      .foreach(strategy => values.remove(values.indexOf(strategy)))
  }

}
