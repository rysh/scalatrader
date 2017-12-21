package domain.strategy

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

  // operation
  private val entry: Boolean  = true
  def entry(size: String): Option[Ordering] = {
    entryPosition = Some(Ordering(size, Margin.size, entry))
    entryPosition
  }
  def close(): Option[Ordering] = {
    val side = domain.reverseSide(entryPosition.get.side)
    val size = entryPosition.map(_.size).getOrElse(Margin.size)
    entryPosition = None
    Some(Ordering(side, size, !entry))
  }

}

class Availability {
  var manualOn = false
  var initialDataLoaded = false
  def isAvailable: Boolean = manualOn && initialDataLoaded
}