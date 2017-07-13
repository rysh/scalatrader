package adapter.aws

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model.Bucket

import scala.collection.JavaConverters

/**
  * Created by ryuhei.ishibashi on 2017/07/11.
  */
class MyS3(region: Regions) {
  val conf = new AwsClientBuilder.EndpointConfiguration("https://s3-us-west-1.amazonaws.com",region.getName)
  val s3: AmazonS3 = AmazonS3ClientBuilder.standard().withEndpointConfiguration(conf).build()

  def listBuckets(): Iterable[Bucket] = {
    JavaConverters.collectionAsScalaIterable(s3.listBuckets())
  }

  def getBucket(name : String) : Option[Bucket] = {
    listBuckets()
      .filter(b => b.getName() == name)
      .collectFirst({case x : Bucket => x})
  }

  def createBucket(name : String): Either[Exception, Bucket] = {
    try {
      getBucket(name) match {
        case Some(bucket) => Right(bucket)
        case None => Right(s3.createBucket(name))
      }
    } catch {
      case e: Exception => Left(e)
    }
  }

  def deleteBucket(name : String): Unit = {
    s3.deleteBucket(name)
  }

}

object MyS3 {
  val region = Regions.US_WEST_1
  def create(): MyS3 = {
    new MyS3(region)
  }
}