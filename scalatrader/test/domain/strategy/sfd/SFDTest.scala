package domain.strategy.sfd

import domain.models.Ticker
import org.scalatest.FunSuite

class SFDTest extends FunSuite {
  import SFD._
  test("deviationRate") {
    assert(newSfd(btc = 1000000.0, btcFx = 1200000.0).deviationRate == 20.0)
    assert(newSfd(btc = 1000000.0, btcFx = 1150000.0).deviationRate == 15.0)
    assert(newSfd(btc = 1000000.0, btcFx = 1100000.0).deviationRate == 10.0)
  }

  val base: BigDecimal = 1000000.0
  test("tooClose") {
    assert(newSfd(btc = base, btcFx = base * (1.1 + unstableRange / 100) + 0.1).tooClose === false)
    assert(newSfd(btc = base, btcFx = base * (1.1 + unstableRange / 100)).tooClose === true)
    assert(newSfd(btc = base, btcFx = base * 1.1).tooClose === true)
    assert(newSfd(btc = base, btcFx = base * (1.1 - unstableRange / 100)).tooClose === true)
    assert(newSfd(btc = base, btcFx = base * (1.1 - unstableRange / 100) - 0.1).tooClose === false)
  }

  test("buyZone") {
    assert(newSfd(btc = base, btcFx = base * (1.1 - targetRange / 100) - 0.1).buyZone === false)
    assert(newSfd(btc = base, btcFx = base * (1.1 - targetRange / 100)).buyZone === true)
    assert(newSfd(btc = base, btcFx = base * 1.1 - 0.1).buyZone === true)
    assert(newSfd(btc = base, btcFx = base * 1.1).buyZone === false)
    assert(newSfd(btc = base, btcFx = base * 1.1 + 0.1).buyZone === false)
  }
  test("sellZone") {
    assert(newSfd(btc = base, btcFx = base * 1.1 - 0.1).sellZone === false)
    assert(newSfd(btc = base, btcFx = base * 1.1).sellZone === false)
    assert(newSfd(btc = base, btcFx = base * 1.1 + 0.1).sellZone === true)
    assert(newSfd(btc = base, btcFx = base * (1.1 + targetRange / 100)).sellZone === true)
    assert(newSfd(btc = base, btcFx = base * (1.1 + targetRange / 100) + 0.1).sellZone === false)
  }

  def newSfd(btc: BigDecimal, btcFx: BigDecimal) = new SFD(newTicker("2018-02-23T10:10:00Z", btc.toDouble), newTicker("2018-02-23T10:10:00Z", btcFx.toDouble))
  def newTicker(timestamp: String, ltp: Double) = Ticker("", timestamp, 0, 0, 0, 0, 0, 0, 0, ltp, 0, 0)
}
