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

}
