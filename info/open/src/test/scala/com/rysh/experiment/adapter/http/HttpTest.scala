package com.rysh.experiment.adapter.http

import cats.data.EitherT
import com.rysh.experiment.adapter.bitflyer.Market
import io.circe
import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.concurrent.Future

class HttpTest extends FunSuite with BeforeAndAfterEach {

  test("testGet") {
    def dec: (String) => Either[circe.Error, List[Market]] = (json:String) => {
      import io.circe.generic.auto._
      import io.circe.parser._
      decode[List[Market]](json)
    }

    val ret: EitherT[Future, circe.Error, List[Market]] =
      Http.get[List[Market]]("https://api.bitflyer.jp/v1/markets", dec)


  }


}
