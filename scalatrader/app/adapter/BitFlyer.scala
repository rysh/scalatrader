package adapter

import java.time.ZonedDateTime

import adapter.aws.{MailContent, SNS, SES}
import adapter.bitflyer.Path._
import domain.ProductCode
import domain.models._
import domain.strategy.turtle.TurtleCore
import domain.util.crypto.HmacSHA256
import io.circe.Printer
import skinny.http.{HTTP, Request}

object BitFlyer {


  def getLatestExecution(): Execution = {
    val body = HTTP.get(BASE + EXECUTIONS, "product_code" -> ProductCode.btcFx, "count" -> 1).textBody

    import io.circe.generic.auto._
    import io.circe.parser._
    decode[List[Execution]](body).map(list => list.head) match {
      case Right(ex) => ex
      case Left(err) => throw err
    }
  }

  def getCollateral(api_key: String, api_secret: String) = {
    val path = COLLATERAL
    val request = Request(BASE + path)
    addSign(request, path, "GET", api_key, api_secret, None)

    import io.circe.generic.auto._
    import io.circe.parser._
    decode[Collateral](HTTP.get(request).textBody) match {
      case Right(ex) => ex
      case Left(err) => throw err
    }
  }

  def getPosition(product_code: String, api_key: String, api_secret: String): Option[Position] = {
    getPositions(api_key, api_secret).get(ProductCode.btcFx)
  }

  def getPositions(api_key: String, api_secret: String) = {
    val path = POSITIONS
    val request = Request(BASE + path)

    addSign(request, path, "GET", api_key, api_secret, None)

    import io.circe.generic.auto._
    import io.circe.parser._
    decode[List[Position]](HTTP.get(request).textBody) match {
      case Right(ex) => Positions(ex)
      case Left(err) => throw err
    }
  }

  def orderByMarket(side: String, size: Double, api_key: String, api_secret: String, dryRun: Boolean): Order = {
    import io.circe.syntax._
    import io.circe.generic.auto._
    val p = Printer.noSpaces.copy(dropNullKeys = true)
    val order = Order(ProductCode.btcFx, "MARKET", side, None, size, 5, "GTC")
    val orderJson = order.asJson.pretty(p)

    if (!domain.isBackTesting) {
      val path = CHILD_ORDER
      val request = Request(BASE + path)
      request.body(orderJson.getBytes("UTF-8"), "application/json")

      addSign(request, path, "POST", api_key, api_secret, Some(orderJson))
      HTTP.post(request)
    }
    //SES.send(MailContent("rysh.cact@gmail.com","info@scalatrader.com", "デモ約定通知", order.toString, order.toString))
    order
  }


  private def addSign(request: Request, path: String, method: String, api_key: String, api_secret: String, body: Option[String]): Unit = {
    val header = request.headers
    val timestamp = ZonedDateTime.now().toEpochSecond.toString
    val data = timestamp + method + path + body.getOrElse("")
    val sign = HmacSHA256.encode(api_secret, data)
    header.put("ACCESS-KEY", api_key)
    header.put("ACCESS-TIMESTAMP", timestamp)
    header.put("ACCESS-SIGN", sign)
    body.map(_ => header.put("Content-Type", "application/json"))
  }
}
