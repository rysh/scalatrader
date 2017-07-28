package adapter

import adapter.aws.S3Store
import adapter.local.PeerConsoleStore
import com.google.gson.JsonElement
import domain.TimeKeeper

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
abstract class Store(
  val timeKeeper: TimeKeeper = TimeKeeper.default()
) {

  /**
    * 受信したデータをメモリ上に保持
    * @param json
    */
  def store(json : JsonElement) : Unit

  /**
    * left: Unit 何もしない
    * right: Store 別名保存用の新しいStoreを生成
    * @return
    */
  def write(): Either[Unit, Store]
}

object Store {
  def s3(bucketName: String) : Store = new S3Store(bucketName)
  def local(bucketName: String) : Store = new S3Store(bucketName)
  def console: Store = PeerConsoleStore
}