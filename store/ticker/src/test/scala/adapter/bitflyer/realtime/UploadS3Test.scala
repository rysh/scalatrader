package adapter.bitflyer.realtime

import adapter.aws.MyS3
import better.files.File
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
  * Created by ryuhei.ishibashi on 2017/07/13.
  */
class UploadS3Test extends FunSuite with BeforeAndAfterAll {

  val bucketName = "scala-trader-upload-s3-test"
  val keyName = "hoge"
  val s3 = MyS3.create()

  override def beforeAll() {
    s3.createBucket(bucketName)
  }
  override def afterAll(): Unit = {
    s3.deleteBucket(keyName)
  }

  test("testUpload") {

    //val file = File("hoge").createIfNotExists()
    //(new UploadS3() {}).upload(bucketName, keyName, file)
  }



}
