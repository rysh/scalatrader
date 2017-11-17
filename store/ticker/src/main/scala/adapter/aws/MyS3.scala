package adapter.aws

import better.files.File
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.{Bucket, PutObjectRequest, PutObjectResult}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}

import scala.collection.JavaConverters

object MyS3 {
  def create(region: Regions): MyS3 = {
    new MyS3(region)
  }
}

class MyS3(region: Regions) {

  val s3: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(region).build()

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
    } catch {case e: Exception => Left(e)}
  }

  def deleteBucket(bucketName : String): Either[Exception, Unit] = {
    try {
      deleteObjectsIn(bucketName)
      s3.deleteBucket(bucketName)
      Right()
    } catch {
      case e: Exception => Left(e)
    }
  }

  private def deleteObjectsIn(bucketName: String) = s3.listObjects(bucketName).getObjectSummaries()
      .forEach(s => s3.deleteObject(bucketName, s.getKey))


  def upload(bucketName:String, keyName: String, file: File): Either[Exception, PutObjectResult] = {
    try {
      Right(s3.putObject(new PutObjectRequest(bucketName, keyName, file.toJava)))
    } catch {case e: Exception => Left(e)}
  }

  def deleteObject(bucketName: String, keyName: String): Either[Exception, Unit] = {
    try {
      s3.deleteObject(bucketName, keyName)
      Right()
    } catch {case e: Exception => Left(e)}
  }
}