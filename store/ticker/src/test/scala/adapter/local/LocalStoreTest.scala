package adapter.local

import java.time.{ZoneId, ZonedDateTime => Time}

import adapter.bitflyer.realtime.TestData
import application.Validations
import com.google.gson.{FieldNamingPolicy, GsonBuilder}
import domain.TimeKeeper
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import testutil.TimeTestHelper

class LocalStoreTest extends FunSuite with BeforeAndAfterAll {

  val now = Time.of(2017, 7, 12 ,4, 30, 14, 11, ZoneId.of("UTC"))
  val sample = new TestData()
  val gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create
  val testing: LocalStore = new LocalStore("LocalStoreTestDummyFile", new TimeKeeper(1, TimeTestHelper.of(2017, 5, 5, 0, 0, 0, 0)))

  override def beforeAll() = Validations.workingDirectoryExisits
  override def afterAll() = testing.delete()

  test("test create & delete") {
    val st = new LocalStore("LocalStoreTestDummy2File")
    assert(st.exists === true)
    st.delete
    assert(st.exists === false)
  }

  test("file name") {
    assert(testing.fileName.replace("LocalStoreTestDummyFile","").size > 0)
  }

  test("testWriteJson") {
    val json = gson.toJsonTree(sample.tickerInfo())
    testing.writeJson(json)

    val lines = testing.lines()
    assert(lines.size === 1)
    assert(lines.mkString("").contains("product_code"))
  }

  test("testStore") {
    testing.write() match {
      case Right(newStore) => assert(newStore !== testing)
      case Left(_) => fail()
    }
  }
}
