package adapter.aws

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.{Bucket, GetObjectRequest}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}

import scala.collection.JavaConverters

object S3 {
  def create(region: Regions): S3 = {
    new S3(region)
  }
}

class S3(region: Regions) {
  def getLines(bucketName: String, key: String): scala.Iterator[String] = {
    val s3object = s3.getObject(new GetObjectRequest(bucketName, key))
    import java.io.BufferedReader
    import java.io.InputStreamReader
    val reader = new BufferedReader(new InputStreamReader(s3object.getObjectContent))
    JavaConverters.asScalaIterator(reader.lines().iterator())
  }


  val s3: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(region).build()

  def listBuckets(): Iterable[Bucket] = {
    JavaConverters.collectionAsScalaIterable(s3.listBuckets())
  }

  def getBucket(name : String) : Option[Bucket] = {
    listBuckets()
      .filter(b => b.getName() == name)
      .collectFirst({case x : Bucket => x})
  }
}