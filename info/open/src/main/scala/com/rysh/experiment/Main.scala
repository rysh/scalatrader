package com.rysh.experiment

import skinny.http.{HTTP, Response}

object Main extends App {
  println("Hello World!")
  // https://api.bitflyer.jp/v1/markets
  // https://api.bitflyer.jp/v1/executions?product_code=BCH_BTC

  val res1: Response = HTTP.get("https://api.bitflyer.jp/v1/markets")
  println(res1.textBody)


  import io.circe.generic.auto._
  import io.circe.parser._
  println(decode[List[Market]](res1.textBody))

  decode[List[Market]](res1.textBody) match {
    case Right(r) => r.foreach(market => {
      val res = HTTP.get("https://api.bitflyer.jp/v1/executions", "product_code" -> market.product_code)
      decode[List[Execution]](res.textBody) match {
        case Right(r2) => r2.foreach(println)
        case _ => {}
      }
    })
    case Left(_) => {}
  }
}

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
