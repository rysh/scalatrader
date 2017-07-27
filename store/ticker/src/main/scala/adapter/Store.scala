package adapter

import adapter.aws.S3Store
import adapter.local.PeerConsoleStore
import com.google.gson.JsonElement
import domain.TimeKeeper

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
abstract class Store(
  tk: TimeKeeper = new TimeKeeper(1)
) {
  var timeKeeper: TimeKeeper = tk
  println(s"Store constructor : ${timeKeeper.time}" )
  /**
    * 受信したデータをメモリ上に保持
    * @param json
    */
  def keep(json : JsonElement) : Unit

  /**
    * left: Unit 何もしない
    * right: Store 別名保存用の新しいStoreを生成
    * @return
    */
  def store(): Either[Unit, Store]
}

object Store {
  def s3(bucketName: String) : Store = new S3Store(bucketName)
  def local(bucketName: String) : Store = new S3Store(bucketName)
  def console: Store = PeerConsoleStore
}