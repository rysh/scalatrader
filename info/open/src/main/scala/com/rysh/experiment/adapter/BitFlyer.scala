package com.rysh.experiment.adapter

import java.time.{Duration, LocalDateTime}

import io.circe
import skinny.http.HTTP


object BitFlyer {

  val CODE_FX_BTC_JPY = "FX_BTC_JPY"

  val URI_executions = "https://api.bitflyer.jp/v1/executions"
  val URI_MARKET = "https://api.bitflyer.jp/v1/markets"

  case class Market(product_code:String, alias: Option[String])

  case class Execution(
    id: Double,
    side: String,
    price: Double,
    size: Double,
    exec_date: String,
    buy_child_order_acceptance_id: String,
    sell_child_order_acceptance_id: String
  )

  implicit class RichBodyText(body: String) {
    import io.circe.generic.auto._
    import io.circe.parser._
    def toMarket(): Either[circe.Error, List[Market]] = decode[List[Market]](body)
    def toExecutions(): Either[circe.Error, List[Execution]] = decode[List[Execution]](body)
  }

  implicit class MarketToExecutions(either: Either[circe.Error, List[Market]]) {
    def toExecutions(): List[Either[circe.Error, (Market, List[Execution])]] = {
      either match {
        case Right(markets) => {
          markets.map(market => marketToExecutions(market).map(list => (market, list)))
        }
        case Left(e) => List(Left(e))
      }
    }

    private def marketToExecutions(market: Market) = {
      HTTP.get(URI_executions, "product_code" -> market.product_code).textBody.toExecutions
    }
  }


  def delta(list: List[Execution], duration: Duration) = {
    new java.text.DecimalFormat("0.0000").format(list.map(e => e.size).sum / (duration.toMillis / 1000))
  }

  def toDuration(times: Seq[LocalDateTime]) = {
    var min = times.last
    var max = times.last
    times.foreach((time: LocalDateTime) => {
      if (time.isBefore(min)) {
        min = time
      }
      if (time.isAfter(max)) {
        max = time
      }
    })
    java.time.Duration.between(min, max)
  }

  def sizeOf(executionsBySide: Map[String, List[Execution]], side: String) = {
    executionsBySide.get(side).map(list => list.size).getOrElse(0)
  }
}
