package adapter.aws

import adapter.Store
import adapter.local.LocalStore
import com.amazonaws.regions.Regions
import domain.{NamingRule, TimeKeeper}

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
class S3Store(val bucketName: String, regions: Regions, override val timeKeeper: TimeKeeper = TimeKeeper.default()) extends LocalStore(bucketName, timeKeeper) {

  override def write(): Either[Unit, Store] = {
    if (!timeKeeper.nowElapsed) {
      return Left()
    }

    // 1分に1回なので都度コネクション生成でよい
    MyS3.create(regions).upload(bucketName, namingRule.s3Path(timeKeeper), file)
    println(s"created $bucketName ${timeKeeper.format("yyyy/MM/dd HH:mm")}")
    delete()
    Right(new S3Store(bucketName, regions, timeKeeper.next))
  }


}