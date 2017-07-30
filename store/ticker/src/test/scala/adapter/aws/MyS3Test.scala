package adapter.aws

import application.Validations
import better.files.File
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
  * Created by ryuhei.ishibashi on 2017/07/13.
  */
class MyS3Test extends FunSuite with BeforeAndAfterAll {
  Validations.workingDirectoryExisits

  val bucketName = s"scala-trader-upload-s3-test-$flake"
  val keyName = s"MyS3Test-$flake"
  val file = File(s"tmp/MyS3Test-$flake").createIfNotExists()
  val s3 = MyS3.create()

  override def beforeAll() = s3.createBucket(bucketName)

  override def afterAll() = {
    s3.deleteBucket(bucketName)
    file.delete()
  }


  test("testUpload") {
    s3.upload(bucketName, keyName, file)
  }


}
