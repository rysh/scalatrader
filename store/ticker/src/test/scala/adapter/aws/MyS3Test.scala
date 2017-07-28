package adapter.aws

import better.files.File
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
  * Created by ryuhei.ishibashi on 2017/07/13.
  */
class MyS3Test extends FunSuite with BeforeAndAfterAll {

  val bucketName = s"scala-trader-upload-s3-test-$flake"
  val keyName = s"hoge-$flake"
  val file = File(s"hoge-$flake").createIfNotExists()
  val s3 = MyS3.create()

  override def beforeAll() = s3.createBucket(bucketName)
  override def afterAll() = s3.deleteBucket(bucketName)


  test("testUpload") {
    s3.upload(bucketName, keyName, file)
  }

  def flake():String = {
    import java.time.format.DateTimeFormatter
    import java.time.{ZoneId, ZonedDateTime}
    ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("mm-ss"))
  }

}
