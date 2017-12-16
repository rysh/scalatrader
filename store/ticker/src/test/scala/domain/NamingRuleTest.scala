package domain

import org.scalatest.FunSuite
import testutil.TimeTestHelper
import domain.NamingRule

class NamingRuleTest extends FunSuite {

  test("testDir") {
    val nr = new NamingRule("tmp")
    assert(nr.dir === "tmp")
  }

  test("testPath") {
    val nr = new NamingRule("tmp")
    assert(nr.path(createTime) == "tmp/201707120430")
  }

  test("testS3Path") {
    val nr = new NamingRule("tmp")
    assert(nr.s3Path(createTime) == "2017/07/12/04/30")

  }

  private def createTime = new TimeKeeper(1, TimeTestHelper.of(2017, 7, 12, 4, 30, 14, 11))

}
