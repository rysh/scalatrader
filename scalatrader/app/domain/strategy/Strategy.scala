package domain.strategy

import domain.Side.Buy
import domain.margin.Margin
import domain.models
import domain.models.{Ticker, Ordering}
import repository.model.scalatrader.User

abstract class Strategy(user: User) {

  val email: String = user.email
  val key: String = user.api_key
  val secret: String = user.api_secret

  // state for system
  val availability = new Availability
  def isAvailable: Boolean = availability.isAvailable


  // state for transaction
  var orderId: Option[String] = None
  var entryPosition: Option[Ordering] = None

  // main logic
  def judgeByTicker(ticker: Ticker): Option[Ordering] = None
  def judgeEveryMinutes(key: Long): Option[Ordering] = None


  // maintenance
  def init():Unit = {
    entryPosition = None
  }
  def putTicker(ticker: models.Ticker): Unit = {}
  def processEvery1minutes():Unit = ()


  //operation
  def entrySize(): Double = Margin.size
  def closeSize(): Double = entryPosition.map(_.size).getOrElse(Margin.size)

  def entry(size: String): Option[Ordering] = {
    entryPosition = Some(Ordering(size, entrySize(), true))
    entryPosition
  }
  def close(): Option[Ordering] = {
    val entrySide = entryPosition.get.side
    entryPosition = None
    Some(Ordering(domain.reverseSide(entrySide), closeSize()))
  }

}

class Availability {
  var manualOn = false
  var initialDataLoaded = false
  def isAvailable: Boolean = manualOn && initialDataLoaded
}