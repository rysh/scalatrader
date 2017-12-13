package domain.strategy.core

import domain.models.Ticker
import org.scalatest.FunSuite

import scala.collection.mutable

class MomentumTest extends FunSuite {

  test("testOf") {
    val candles = DummyCandle.createCandle
    val momentum = new Momentum(candles, 1, 10)
    momentum.loadAll()
    //momentum.values.foreach(println)
  }

}
