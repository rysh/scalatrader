package application

import adapter.BitFlyer.MyExecution
import com.typesafe.config.ConfigFactory
import domain.Side._
import org.scalatest.FunSuite
import play.api.Configuration

class PerformanceViewApplicationTest extends FunSuite {
  scalikejdbc.config.DBs.setupAll
  val target = new PerformanceViewApplication(new Configuration(ConfigFactory.load()))

  test("amount of money") {

    val a = Seq(execution(100, 1, Buy), execution(200, 1, Buy))
    assert(target.amountOfMoney(a) === 300)
  }

  test("margin gain") {

    def doTest(entryPrice: Int, entrySide: String, closePrice: Int, closeSide: String, expected: Int) = {
      val entry = Seq(execution(entryPrice, 1, entrySide))
      val close = Seq(execution(closePrice, 1, closeSide))
      assert(target.marginGain(entry, close) === expected)
    }

    doTest(100, Buy, 200, Sell, 100)
    doTest(200, Buy, 100, Sell, -100)
    doTest(200, Sell, 400, Buy, -200)
    doTest(400, Sell, 200, Buy, 200)

    val a = Seq(execution(100, 1, Buy), execution(200, 1, Buy))
    val b = Seq(execution(200, 2, Sell))
    assert(target.marginGain(b, a) === 100)

    val c = Seq(execution(100, 1, Sell), execution(200, 1, Sell))
    val d = Seq(execution(200, 2, Buy))
    assert(target.marginGain(c, d) === -100)
  }

  test("amount of price") {

    val a = Seq(execution(100, 1, Buy), execution(200, 1, Buy))
    assert(target.amountOfPrice(a) === 150)
  }

  test("margin price gain") {
    val a = Seq(execution(100, 0.5, Buy), execution(200, 0.5, Buy))
    val b = Seq(execution(200, 1, Sell))
    assert(target.marginPriceGain(b, a) === 50)

    val c = Seq(execution(100, 0.5, Sell), execution(200, 0.5, Sell))
    val d = Seq(execution(200, 1, Buy))
    assert(target.marginPriceGain(c, d) === -50)
  }

  private def execution(price: BigDecimal, size: BigDecimal, side: String) = {
    MyExecution(0, side, price.toDouble, size.toDouble, "", "", 0, "")
  }
}
