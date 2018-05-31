package domain.strategy.dtn

import domain.strategy.core.Bar

class Dtn(values: Iterable[Bar]) {
  val last2 = values.takeRight(2)
  val currentBar = last2.last
  val referenceBar: Bar = last2.head
  val k = 1.6
  val candles = values.take(5)

  val avgRange = candles.map(b => (b.high - b.low).abs).sum / 5

  def breakLong = {
    _breakLong && !_breakShort
  }

  private def _breakLong = {
    val targetPrice = referenceBar.open + avgRange * k
    targetPrice < referenceBar.high
  }

  def breakShort: Boolean = {
    _breakShort && !_breakLong
  }

  private def _breakShort = {
    val targetPrice = referenceBar.open - avgRange * k
    targetPrice > referenceBar.low
  }

  def toLossCut = new DtnLossCut(candles, referenceBar, currentBar, avgRange)

  override def toString = s"Dtn($referenceBar, $candles, $breakLong, $breakShort)"
}
