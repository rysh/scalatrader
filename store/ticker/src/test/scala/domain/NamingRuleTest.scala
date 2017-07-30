package domain

import org.scalatest.FunSuite
import testutil.TimeTestHelper

class NamingRuleTest extends FunSuite {

  test("testDir") {
    assert(NamingRule.dir === "tmp")
  }

  test("testPath") {
    assert(NamingRule.path(createTime) == "tmp/ticker-201707120430")
  }

  test("testS3Path") {
    assert(NamingRule.s3Path(createTime) == "2017/07/12/04/30")

  }

  private def createTime = new TimeKeeper(1, TimeTestHelper.of(2017, 7, 12, 4, 30, 14, 11))

}
