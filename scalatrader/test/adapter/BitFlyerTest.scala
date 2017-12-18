package adapter

import org.scalatest.FunSuite

class BitFlyerTest extends FunSuite {
  import BitFlyer._

  val key = "###"
  val secret = "###"
  ignore("") {
    val executions: Seq[MyExecution] = myExecutions(key, secret)
    executions.reverse.foreach(e => {
      val price = (e.price * e.size * (if (e.side == domain.Side.Sell) -1 else 1)).toLong


      println(s"[${e.side}] ${e.exec_date} price ${e.price} size ${e.size}")
    })
  }

}
