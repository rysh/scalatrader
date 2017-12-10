package domain.time

import org.scalatest.FunSuite

class DateUtilTest extends FunSuite {

  test("parseKeyOfUnitSeconds") {
    val ret = DateUtil.parseKey(20171208101025L)
    assert(ret.isEqual(DateUtil.fromTimestamp("2017-12-08 10:10:25 +0000")))
  }

}
