package application

import domain.strategy.core.Bar
import org.scalatest.FunSuite

class RealTimeReceiverTest extends FunSuite {

  test("testInit") {

    var i = 0
    try {
      retry(3, () => {
        i += 1
        throw new Exception
      })
    } catch {
      case e: Exception => assert(i == 3)
    }
  }

  def retry(times: Int, func: () => Unit): Unit = {
    var i = 0
    while (i < times) {
      i += 1
      try {
        func()
        i = times
      } catch {
        case e: Exception => if (i == times) throw e
      }
      if (i < times) {
        Thread.sleep(2000)
      }
    }
  }
}
