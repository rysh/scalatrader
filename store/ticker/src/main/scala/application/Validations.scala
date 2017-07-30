package application

import adapter.aws.MyS3
import better.files.File

/**
  * Created by ryuhei.ishibashi on 2017/07/13.
  */
object Validations {
  def bucketExists(bucketName: String) = MyS3.create().createBucket(bucketName)
  def workingDirectoryExisits = {
    val tmp = File("tmp")
    if (!tmp.exists) {
      tmp.createDirectory()

    }
  }
}
