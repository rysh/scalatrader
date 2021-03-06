package adapter.aws

import java.time.{ZonedDateTime => Time}

import adapter.bitflyer.realtime.TestData
import application.Validations
import com.amazonaws.regions.Regions
import com.google.gson.{GsonBuilder, FieldNamingPolicy}
import domain.TimeKeeper
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import testutil.TimeTestHelper

/**
  * Created by ryuhei.ishibashi on 2017/07/12.
  */
class S3StoreTest extends FunSuite with BeforeAndAfterAll {

  val now = TimeTestHelper.of(2017, 7, 12, 4, 30, 14, 11)
  val s3Store: S3Store = new S3Store("tmp", Regions.US_WEST_1, new TimeKeeper(1, now))
  val sample = new TestData()
  val gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create

  override def beforeAll() = Validations.workingDirectoryExisits("tmp")
  override def afterAll() = s3Store.delete()

  test("testStore") {

    s3Store.writeJson(gson.toJsonTree(sample.tickerInfo()))
    assert(s3Store.lines.mkString("").contains(sample.jsonString))
  }

}
