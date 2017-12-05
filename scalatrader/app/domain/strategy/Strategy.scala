package domain.strategy

import domain.models
import domain.models.Ticker

trait Strategy {

  def email:String

  def putTicker(ticker: models.Ticker)
  def judgeByTicker(ticker: Ticker): Option[(String, Double)] = None
  def judgeEveryMinutes(key: Long): Option[(String, Double)] = None
  def loadInitialData(initialData: Seq[(Long, Iterator[String])]): Unit

  def processEvery1minutes() = {

  }

}
