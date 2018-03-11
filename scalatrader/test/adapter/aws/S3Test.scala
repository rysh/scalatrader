package adapter.aws

import java.util.stream.Collectors

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.{GetObjectRequest, Bucket}
import org.scalatest.FunSuite

import scala.collection.JavaConverters

class S3Test extends FunSuite {

  test("testListBuckets") {
    //val s3 = S3.create(Regions.US_WEST_1)
    //val hoge: Iterator[String] = s3.getLines("btcfx-ticker-scala", "2017/11/23/01/01")

  }

}
