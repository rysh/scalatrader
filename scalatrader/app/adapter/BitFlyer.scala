package adapter

import java.time.ZonedDateTime

import adapter.bitflyer.Path._
import adapter.bitflyer.models.{Position, Execution, Collateral}
import domain.util.crypto.HmacSHA256
import skinny.http.{HTTP, Request}

object BitFlyer {

  def getLatestExecution(): Execution = {
    val body = HTTP.get(BASE + EXECUTIONS, "product_code" -> "FX_BTC_JPY", "count" -> 1).textBody

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
    addSign(request, path, api_key, api_secret, None)

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

    addSign(request, path, api_key, api_secret, None)

    import io.circe.generic.auto._
    import io.circe.parser._
    decode[List[Position]](HTTP.get(request).textBody) match {
      case Right(ex) => ex
      case Left(err) => throw err
    }
  }


  private def addSign(request: Request, path: String, api_key: String, api_secret: String, body: Option[String]) = {
    val header = request.headers
    val timestamp = ZonedDateTime.now().toEpochSecond.toString
    val data = timestamp + "GET" + path + body.getOrElse("")
    val sign = HmacSHA256.encode(api_secret, data)
    header.put("ACCESS-KEY", api_key)
    header.put("ACCESS-TIMESTAMP", timestamp)
    header.put("ACCESS-SIGN", sign)
  }
}
