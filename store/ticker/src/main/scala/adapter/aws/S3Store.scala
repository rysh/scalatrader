package adapter.aws

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import adapter.Store
import better.files.File
import com.google.gson.JsonElement

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
class S3Store(bucketName: String) extends Store with UploadS3 {
  val file = fileOfTime()
  val time = ZonedDateTime.now(ZoneId.of("UTC"))

  override def store(json: JsonElement): Unit = {
    writeJson(file, json)
  }

  override def write(): Either[Unit, Store] = {
    if (!timeKeeper.nowElapsed) {
      return Left()
    }
    timeKeeper = timeKeeper.next

    val keyName = pathForS3(time)
    upload(bucketName, keyName, file)
    file.delete()
    Right(new S3Store(bucketName))

  }

  def writeJson(file:File, json: JsonElement): Unit ={
    import better.files.Dsl.SymbolicOperations
    file << json.toString
  }

  def fileOfTime(): File = {
    import better.files._
    File(localName(ZonedDateTime.now(ZoneId.of("UTC")))).createIfNotExists()
  }

  def localName(now: ZonedDateTime):String = {
    now.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_hh_mm"))
  }

  def pathForS3(now: ZonedDateTime):String = {
    now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd/hh/mm"))
  }

}
