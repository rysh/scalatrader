package adapter.aws

import adapter.Store
import adapter.local.LocalStore
import domain.{NamingRule, TimeKeeper}

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
class S3Store(
   val bucketName: String,
   override val timeKeeper:TimeKeeper = TimeKeeper.default()
) extends LocalStore(timeKeeper = timeKeeper) {

  override def write(): Either[Unit, Store] = {
    if (!timeKeeper.nowElapsed) {
      return Left()
    }

    // 1分に1回なので都度コネクション生成でよい
    MyS3.create().upload(bucketName, NamingRule.s3Path(timeKeeper), file)
    delete()
    Right(new S3Store(bucketName, timeKeeper.next))
  }


}