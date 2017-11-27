package domain.backtest

import java.time.ZonedDateTime

import domain.models
import domain.models.Ticker

import scala.collection.mutable

object WaitingOrder {
  val orderRequestDelay = 3 // seconds

  val waitingExecuted = new mutable.HashMap[String, WaitingOrder]()

  def request(email:String, ticker: Ticker, order: models.Order) = {
    val estimatedTime = ZonedDateTime.parse(ticker.timestamp).plusSeconds(orderRequestDelay)
    waitingExecuted.put(email, WaitingOrder(estimatedTime, order))
  }

  val waiting: Boolean = true
  val notWating: Boolean = false

  def isWaitingOrJustExecute(email:String, ticker: Ticker, func: models.Order => Unit): Boolean = {
    waitingExecuted.get(email) match {
      case Some(w) => {
        if (ZonedDateTime.parse(ticker.timestamp).isBefore(w.estimatedTime)) {
          waiting
        } else {
          waitingExecuted.remove(email)
          func.apply(w.order)
          notWating
        }
      }
      case None => notWating
    }
  }
}

case class WaitingOrder(estimatedTime: ZonedDateTime, order: models.Order)