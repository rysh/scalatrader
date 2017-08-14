package com.rysh.experiment

import com.rysh.experiment.adapter.bitflyer.{Execution, Market}
import skinny.http.HTTP

object Main extends App {

  // https://api.bitflyer.jp/v1/markets
  // https://api.bitflyer.jp/v1/executions?product_code=BCH_BTC

  val request = HTTP.get("https://api.bitflyer.jp/v1/markets")


  import io.circe.generic.auto._
  import io.circe.parser._

  decode[List[Market]](request.textBody) match {
    case Right(list) => list.foreach(execMarket)
    case Left(_) => {}
  }

  def execMarket(market: Market) = {
    val request = HTTP.get("https://api.bitflyer.jp/v1/executions", "product_code" -> market.product_code)
    decode[List[Execution]](request.textBody) match {
      case Right(r2) => r2.foreach(println)
      case _ => {}
    }
  }
}



