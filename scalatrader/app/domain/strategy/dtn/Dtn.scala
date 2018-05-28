package domain.strategy.dtn

import domain.strategy.core.Bar

class Dtn(values: Iterable[Bar]) {
  val last2 = values.takeRight(2)
  //    val currentBar = last2.last
  val referenceBar: Bar = last2.head
  val k = 1.6
  val candles = values.take(5)

  val avgRange = candles.map(b => (b.high - b.low).abs).sum / 5

  def breakLong = {
    val targetPrice = referenceBar.open + avgRange * k
//    println(s"targetPrice[$targetPrice] = referenceBar.open[${referenceBar.open}] + avgRange[$avgRange] * k")
//    println(s"targetPrice[$targetPrice] < referenceBar.low[${referenceBar.high}]")
    targetPrice < referenceBar.high
  }
  def breakShort = {
    val targetPrice = referenceBar.open - avgRange * k
//    println(s"targetPrice[$targetPrice] = referenceBar.open[${referenceBar.open}] - avgRange[$avgRange] * k")
//    println(s"targetPrice[$targetPrice] > referenceBar.low[${referenceBar.low}]")
    targetPrice > referenceBar.low
  }

  override def toString = s"Dtn($referenceBar, $candles, $breakLong, $breakShort)"
}
