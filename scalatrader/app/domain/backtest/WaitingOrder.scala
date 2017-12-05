package domain.backtest

import java.time.ZonedDateTime

import domain.models
import domain.models.Ticker

import scala.collection.mutable

object WaitingOrder {
  val orderRequestDelay = 3 // seconds

  val waitingExecuted = new mutable.HashMap[String, WaitingOrder]()

  def request(email:String, time: ZonedDateTime, order: models.Order) = {
    val estimatedTime = time.plusSeconds(orderRequestDelay)
    waitingExecuted.put(email, WaitingOrder(estimatedTime, order))
  }

  val waiting: Boolean = true
  val notWaiting: Boolean = false

  def isWaiting(email:String, time: ZonedDateTime): Boolean = {
    waitingExecuted.get(email) match {
      case Some(w) => {
        if (time.isBefore(w.estimatedTime)) {
          waiting
        } else {
          notWaiting
        }
      }
      case None => notWaiting
    }
  }

  def isWaitingOrJustExecute(email:String, time: ZonedDateTime, func: models.Order => Unit): Boolean = {
    waitingExecuted.get(email) match {
      case Some(w) => {
        if (time.isBefore(w.estimatedTime)) {
          waiting
        } else {
          waitingExecuted.remove(email)
          func.apply(w.order)
          notWaiting
        }
      }
      case None => notWaiting
    }
  }
}

case class WaitingOrder(estimatedTime: ZonedDateTime, order: models.Order)