package domain.strategy.momentum

import domain.models
import domain.strategy.Strategy
import repository.model.scalatrader.User

class MomentumStrategy(user: User) extends Strategy {
  override def email: String = user.email

  override def putTicker(ticker: models.Ticker): Unit = {

  }

  override def judge(ticker: models.Ticker): Option[(String, Double)] = {
    None
  }

  override def loadInitialData(initialData: Seq[(Long, Iterator[String])]): Unit = {

  }
}
