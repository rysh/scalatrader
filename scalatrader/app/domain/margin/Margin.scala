package domain.margin

import domain.models.Positions

class Margin(
             /** 預入証拠金 */
             depositMargin: Double,
             positions: Positions,
             ltp: Double) {

  /** 建玉評価損益 */
  def marketValuationGain(): Long = {
    marketValuationGain(ltp)
  }
  def marketValuationGain(ltp: Double): Long = {
    positions.values.map(p => (ltp - p.price) * p.size * (if (p.side == "SELL") -1 else 1)).sum.toLong
  }

  /** 必要証拠金 */
  def requiredMargin: Long = {
    positions.values.map(p => p.price * p.size * 0.067).sum.toLong
  }

  /** 評価証拠金 */
  def evaluationMargin: Long = {
    depositMargin.toLong + marketValuationGain
  }

  /**
    * 証拠金維持率
    * 評価証拠金 ÷（必要証拠金）
    * */
  def marginMaintenanceRate: Double = {
    evaluationMargin / requiredMargin * 100
  }

  def lossCutLine: Option[Long] = {
    //(depositMargin + marketValuationGain) / requiredMargin * 100 = 50
    //marketValuationGain = 50 / 100 * requiredMargin - depositMargin
    val requiredMarketValuationGain = 50 / 100 * requiredMargin - depositMargin

    var lossCutLine = ltp
    while (requiredMarketValuationGain < marketValuationGain(lossCutLine)) {
      lossCutLine = lossCutLine - 5000
    }

    if (lossCutLine == ltp) {
      None
    } else {
      Some((lossCutLine + 5000).toLong)
    }
  }

  def sizeOf1x: Double = (evaluationMargin / ltp * 100).toLong / 100.0

}

object Margin {
  def resetSize(): Unit = {
    sizeUnit = defaultSizeUnit
    leverage = defaultLeverage
  }

  val defaultSizeUnit = 0.2
  val defaultLeverage = 1.5
  var sizeUnit: Double = defaultSizeUnit
  var leverage: Double = defaultLeverage
  def size(leverage: Double): Double = {
    if (leverage >= 0) {
      (sizeUnit * leverage * 100).ceil / 100
    } else {
      0.01
    }
  }
}
