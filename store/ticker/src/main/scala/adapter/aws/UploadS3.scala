package adapter.aws

import better.files.File
import com.amazonaws.AmazonServiceException
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}

/**
  * Created by ryuhei.ishibashi on 2017/07/13.
  */
trait UploadS3 {

  def upload(bucketName: String, keyName: String, file: File) = {
    try {
      val s3: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_1).build()
      s3.putObject(new PutObjectRequest(bucketName, keyName, file.toJava))
    } catch {
      case e: AmazonServiceException =>
        println(e.getErrorMessage)
        System.exit(1)
    }
  }
}
