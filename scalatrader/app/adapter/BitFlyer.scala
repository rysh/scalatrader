package adapter

import java.net.URLEncoder
import java.time.ZonedDateTime

import adapter.bitflyer.Path._
import com.google.gson.Gson
import domain.ProductCode
import domain.models._
import domain.util.crypto.HmacSHA256
import io.circe.{Json, Printer}
import skinny.http.{HTTP, Request}

object BitFlyer {


  def getLatestExecution(): Execution = {
    val body = HTTP.get(BASE + EXECUTIONS, "product_code" -> ProductCode.btcFx", "count" -> 1).textBody

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

  def orderByMarket(side: String, size: Double, api_key: String, api_secret: String) = {
    import io.circe.syntax._
    import io.circe.generic.auto._
    val p = Printer.noSpaces.copy(dropNullKeys = true)
    val orderJson = Order(ProductCode.btcFx, "MARKET", side, None, size, 5, "GTC").asJson.pretty(p)

    val path = CHILD_ORDER
    val request = Request(BASE + path)
    request.body(orderJson.getBytes("UTF-8"), "application/json")

    addSign(request, path, "POST", api_key, api_secret, Some(orderJson))
    val response = HTTP.post(request)
    println(response.textBody)
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
