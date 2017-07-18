package application

import adapter.aws.MyS3

/**
  * Created by ryuhei.ishibashi on 2017/07/13.
  */
object Validations {
  def bucketExists(bucketName: String) = MyS3.create().createBucket(bucketName)
}
