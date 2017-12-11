package domain.time

import java.time.ZonedDateTime

import DateUtil._
import org.scalatest.FunSuite

class DateUtilTest extends FunSuite {

  test("parseKeyOfUnitSeconds") {
    val ret = parseKey(20171208101025L)
    assert(ret.isEqual(fromTimestamp("2017-12-08 10:10:25 +0000")))
  }

  test("keyOf") {
    assert(keyOf(fromTimestamp("2017-12-01 00:00:05 +0000"), 60) === 20171201000000L)
    assert(keyOf(fromTimestamp("2017-12-01 00:00:05 +0000"), 300) === 20171201000000L)
    assert(keyOf(fromTimestamp("2017-12-01 00:02:05 +0000"), 300) === 20171201000000L)
    assert(keyOf(fromTimestamp("2017-12-01 00:00:13 +0000"), 10) === 20171201000010L)
    assert(keyOf(fromTimestamp("2017-12-01 00:00:33 +0000"), 20) === 20171201000020L)
  }
}
