package domain.strategy.dtn

import domain.models.Ticker
import domain.strategy.core.Bar

case class DtnLossCut(candles: Iterable[Bar], referenceBar: Bar, entryBar: Bar, avgRange: Double) {
  val upperLossCutLine = candles.map(_.high).max + avgRange
  val underLossCutLine = candles.map(_.low).min - avgRange
  def leaveRangeOnUpperSide(ticker: Ticker): Boolean = upperLossCutLine < ticker.ltp
  def leaveRangeOnUnderSide(ticker: Ticker): Boolean = underLossCutLine > ticker.ltp

}
