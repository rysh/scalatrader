package com.rysh.experiment

import java.text.DecimalFormat
import java.time.{LocalDateTime, Duration}

import com.rysh.experiment.adapter.BitFlyer._
import com.rysh.experiment.util.DateTimeUtil
import io.circe
import skinny.http.{HTTP, Response}

object Main extends App {

  def notice(error: circe.Error) = println(error)

  def summarize(res: (Market, List[Execution])) = {
    val (market, list) = res
    val times: Seq[LocalDateTime] = list.map(e => LocalDateTime.parse(e.exec_date))
    val executionsBySide: Map[String, List[Execution]] = list.groupBy(_.side)

    val code = market.product_code
    val buy = sizeOf(executionsBySide, "BUY")
    val sell = sizeOf(executionsBySide, "SELL")
    val duration = toDuration(times)
    val timeRange = DateTimeUtil.formatDuration(duration)
    val prices = list.map(e => e.price)
    val median = prices.sortWith(_ < _).drop(prices.length / 2).head
    val deltaOfSize = delta(list, duration)
    s"""========================
       |${code}
       |BUY/SELL: ${buy}/${sell},
       |price   : ${median},
       |delta   : ${deltaOfSize},
       |range   : ${timeRange}
       |=========================""".stripMargin
  }

  HTTP.get(URI_MARKET).textBody.toMarket
    .map(list => list.filter(e => e.product_code == CODE_FX_BTC_JPY))
    .toExecutions
    .foreach(_ match {
    case Right(result) => println(summarize(result))
    case Left(error) => notice(error)
  })

}



