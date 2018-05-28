package domain.strategy.dtn

import domain.strategy.core.Bar
import org.scalatest.FunSuite

class DtnTest extends FunSuite {

  var no = 0
  def bar(open: Double, low: Double, high: Double, close: Double) = {
    no = no + 1
    val b = new Bar(no)
    b.put(open)
    b.put(low)
    b.put(high)
    b.put(close)
    b
  }

  test("testNotBreak") {
    val hoge: Seq[Bar] = Seq(
      bar(110, 100, 120, 110),
      bar(110, 100, 120, 110),
      bar(110, 100, 120, 110),
      bar(110, 100, 120, 110),
      bar(110, 100, 120, 110),
      bar(110, 100, 142, 110),
      bar(110, 100, 120, 110)
    )
    val dtn = new Dtn(hoge)
    assert(!dtn.breakLong)
    assert(!dtn.breakShort)
  }

  test("testBreakLong") {
    val hoge: Seq[Bar] = Seq(
      bar(110, 100, 120, 110),
      bar(110, 100, 120, 110),
      bar(110, 100, 120, 110),
      bar(110, 100, 120, 110),
      bar(110, 100, 120, 110),
      bar(110, 100, 142.1, 110),
      bar(110, 100, 120, 110)
    )
    val dtn = new Dtn(hoge)
    assert(dtn.breakLong)
    assert(!dtn.breakShort)
  }

  test("testBreakShort") {
    val hoge: Seq[Bar] = Seq(
      bar(110, 100, 120, 110),
      bar(110, 100, 120, 110),
      bar(110, 100, 120, 110),
      bar(110, 100, 120, 110),
      bar(110, 100, 120, 110),
      bar(110, 100, 77.9, 110),
      bar(110, 100, 120, 110)
    )
    val dtn = new Dtn(hoge)
    assert(!dtn.breakLong)
    assert(dtn.breakShort)
  }

  test("test") {
    val hoge: Seq[Bar] = Seq(
      bar(853871, 853649, 854364, 854219),
      bar(854229, 854229, 854745, 854572),
      bar(854634, 854267, 854634, 854269),
      bar(854269, 854200, 854594, 854507),
      bar(854507, 853951, 854508, 854382),
      bar(854390, 853400, 854436, 853621),
      bar(110, 100, 120, 110)
    )
    val dtn = new Dtn(hoge)
    assert(!dtn.breakLong)
    assert(!dtn.breakShort)
  }

}
