package domain

import java.time.temporal.ChronoUnit

import org.scalatest.FunSuite

class TimeKeeperTest extends FunSuite {
  import testutil.TimeTestHelper._

  test("二つの時間を受け取って指定されたintervalの間隔が空いているか判定する") {
    var tk = new TimeKeeper(1, of(2017,7,5,14,0, 0, 0))
    assert(tk.isElapsed(of(2017,7,5,14,0, 0, 0)) === false)
    assert(tk.isElapsed(of(2017,7,5,14,1, 0, 0)) === true)
  }

  test("秒の単位を切り捨てて基準時間とする") {
    val tk = new TimeKeeper(1,of(2017,7,5,14,0, 15, 0))
    assert(tk.isElapsed(of(2017,7,5,14,1, 0, 0)) === true)
  }


  test("新しい基準時間を設定する") {
    var tk = new TimeKeeper(1)
    tk = tk.lap(of(2017, 7, 5, 14, 0, 15, 0))
    val time1 = of(2017, 7, 5, 14, 1, 13, 0)
    assert(tk.isElapsed(time1) === true)
    tk = tk.lap(time1)
    assert(tk.isElapsed(time1) === false)
    assert(tk.isElapsed(time1.plus(30, ChronoUnit.SECONDS)) === false)
    assert(tk.isElapsed(time1.plus(60, ChronoUnit.SECONDS)) === true)
  }


  test("formatの確認") {
    var tk = new TimeKeeper(1)
    tk = tk.lap(of(2017, 7, 5, 14, 0, 15, 0))
    val time1 = of(2017, 7, 5, 14, 1, 13, 0)
    assert(tk.format("yyyy/MM/dd/HH/mm") === "2017/07/05/14/00")
    tk = tk.lap(time1)
    assert(tk.format("yyyy/MM/dd/HH/mm") === "2017/07/05/14/01")
  }
}


