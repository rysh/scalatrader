package application

import adapter.aws.MyS3
import better.files.File
import com.amazonaws.regions.Regions

/**
  * Created by ryuhei.ishibashi on 2017/07/13.
  */
object Validations {
  def bucketExists(bucketName: String, region: Regions): Either[Exception, Any] = MyS3.create(region).createBucket(bucketName)
  def workingDirectoryExisits(bucketName: String) = {
    val tmp = File(bucketName)
    if (!tmp.exists) {
      tmp.createDirectory()
    }
  }
}
