package domain.strategy.turtle

import scala.collection.mutable


object BackTestResults {

  val values = new mutable.ArrayBuffer[OrderResult]

  def add(order: OrderResult): Unit = {
    values += order
  }

  case class OrderResult(timestamp:String, side:String, price:Double, size: Double)


  def report() = {
    var total: Double = 0.0
    var entry: Option[OrderResult] = None
    values.foreach(o => {
      println(o)
      if (entry.isEmpty) {
        entry = Some(o)
      } else {
        val value = entry.map(e => o.price - e.price).map(s => s * (if (o.side == "SELL") 1 else -1)).get
        total = total + value
        println(s"損益 $value : 累積損益 $total")
        entry = None
      }
    })
    println(s"最終損益 $total")
  }
}
