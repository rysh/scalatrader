package adapter.bitflyer.realtime

import java.time.{ZoneId, ZonedDateTime => Time}

import better.files.File
import com.google.gson.{FieldNamingPolicy, GsonBuilder}
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
  * Created by ryuhei.ishibashi on 2017/07/12.
  */
class StoreS3Test extends FunSuite with BeforeAndAfterAll {

  val s3 = new StoreS3("btcfx-ticker-scala-test")
  val now = Time.of(2017,7,12,4,30,14,11, ZoneId.of("UTC"))
  val sample = new TestData()
  val gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create

  val dummyFile = File("hoge").createIfNotExists()
  override def afterAll() {
    dummyFile.delete()
  }

  test("testFileNameFromNow") {
    assert(s3.localName(now) == "2017_07_12_04_30")
  }

  test("testPathForS3") {
    assert(s3.pathForS3(now) == "2017/07/12/04/30")
  }

  test("testStore") {
    s3.write(dummyFile, gson.toJsonTree(sample.tickerInfo()))
    assert(dummyFile.lines.mkString("").contains(sample.jsonString))
  }
  test("pathの更新とデータの永続化") {
    ???
  }

}
