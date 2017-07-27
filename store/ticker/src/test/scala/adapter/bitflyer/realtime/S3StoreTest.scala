package adapter.bitflyer.realtime

import java.time.{ZoneId, ZonedDateTime => Time}

import adapter.aws.S3Store
import better.files.File
import com.google.gson.{FieldNamingPolicy, GsonBuilder}
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
  * Created by ryuhei.ishibashi on 2017/07/12.
  */
class S3StoreTest extends FunSuite with BeforeAndAfterAll {

  val s3Store: S3Store = new S3Store("btcfx-ticker-scala-test")
  val now = Time.of(2017,7,12,4,30,14,11, ZoneId.of("UTC"))
  val sample = new TestData()
  val gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create

  val dummyFile = File("hoge").createIfNotExists()

  override def afterAll() = dummyFile.delete()


  test("testFileNameFromNow") {
    assert(s3Store.localName(now) == "2017_07_12_04_30")
  }

  test("testPathForS3") {
    assert(s3Store.pathForS3(now) == "2017/07/12/04/30")
  }

  test("testStore") {
    s3Store.writeJson(dummyFile, gson.toJsonTree(sample.tickerInfo()))
    assert(dummyFile.lines.mkString("").contains(sample.jsonString))
  }
  test("pathの更新とデータの永続化") {
    ??? //TODO
  }
}
