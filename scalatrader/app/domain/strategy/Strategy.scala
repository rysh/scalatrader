package domain.strategy

import domain.models
import domain.models.{Ticker, Ordering}

trait Strategy {

  def email:String
  def key:String
  def secret:String

  val availability = new Availability
  var orderId:Option[String] = None
  def isAvailable = availability.isAvailable
  var entryPosition: Option[Ordering] = None

  def putTicker(ticker: models.Ticker)
  def judgeByTicker(ticker: Ticker): Option[Ordering] = None
  def judgeEveryMinutes(key: Long): Option[Ordering] = None

  def processEvery1minutes():Unit = {

  }
  def init():Unit

}

class Availability {
  var manualOn = false
  var initialDataLoaded = false
  def isAvailable = manualOn && initialDataLoaded
}