package domain.strategy

import domain.models
import domain.models.Ticker

trait Strategy {

  def email:String

  def putTicker(ticker: models.Ticker)
  def judge(ticker: Ticker): Option[(String, Double)]
  def loadInitialData(initialData: Seq[(Long, Iterator[String])]): Unit

  def processEvery1minutes() = {

  }

}
