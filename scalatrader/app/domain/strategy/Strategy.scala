package domain.strategy

import domain.models
import domain.models.{Ticker, Ordering}

trait Strategy {

  def email:String
  def key:String
  def secret:String

  var isAvailable = false

  def putTicker(ticker: models.Ticker)
  def judgeByTicker(ticker: Ticker): Option[Ordering] = None
  def judgeEveryMinutes(key: Long): Option[Ordering] = None

  def processEvery1minutes():Unit = {

  }
  def init():Unit

}
