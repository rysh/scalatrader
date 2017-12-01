package domain.margin

import domain.models.{Position, Positions}
import org.scalatest.FunSuite

class MarginTest extends FunSuite {

  def pos(
     side: String,
     price: Double,
     size: Double): Position = {
    Position("FX_BTC_JPY", side, price, size, 0, 0, 0, null, 15, 0)
  }

  test("testMarketValuationGain") {
    val depositMargin = 200000
    val positions = Positions(Seq(pos("BUY", 1010000, 0.2), pos("SELL", 1010000, 0.2)))
    val ltp: Long = 1000000
    val margin = new Margin(depositMargin, positions, ltp)
    assert(margin.marketValuationGain === 0)
  }

  test("testRequiredMargin") {
    val depositMargin = 200000
    val positions = Positions(Seq(pos("BUY", 1010000, 0.2), pos("SELL", 1010000, 0.2)))
    val ltp: Long = 1000000
    val margin = new Margin(depositMargin, positions, ltp)
    assert(margin.requiredMargin === 27068)
  }

  test("testEvaluationMargin") {
    val depositMargin = 200000
    val positions = Positions(Seq(pos("BUY", 1010000, 0.2)))
    val ltp: Long = 1000000
    val margin = new Margin(depositMargin, positions, ltp)
    assert(margin.evaluationMargin === 198000)
  }


  test("testMarginMaintenanceRate") {
    val depositMargin = 200000
    val positions = Positions(Seq(pos("BUY", 1010000, 0.2)))
    val ltp: Long = 1000000
    val margin = new Margin(depositMargin, positions, ltp)
    println(margin.marginMaintenanceRate)
    assert(margin.marginMaintenanceRate === 1400)
  }
  test("testLossCutLine") {
    val depositMargin = 200000
    val positions = Positions(Seq(pos("BUY", 1010000, 0.2)))
    val ltp: Long = 1000000
    val margin = new Margin(depositMargin, positions, ltp)
    assert(margin.lossCutLine.get === 15000)
  }

  test("testLossCutLine2") {
    val depositMargin = 200000
    val positions = Positions(Seq(pos("BUY", 1010000, 1)))
    val ltp: Long = 810000
    val margin = new Margin(depositMargin, positions, ltp)
    assert(margin.lossCutLine === None)
  }
}
