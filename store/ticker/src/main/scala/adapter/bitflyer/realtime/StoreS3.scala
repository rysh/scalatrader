package adapter.bitflyer.realtime

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import better.files.File
import com.google.gson.JsonElement

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
class StoreS3 extends Store with UploadS3 {
  val file = fileOfTime()
  val time = ZonedDateTime.now(ZoneId.of("UTC"))

  override def keep(json: JsonElement): Unit = {
    write(file, json)
  }

  override def store(): Unit = {
    val bucketName = "btcfx-ticker-s"
    val keyName = pathForS3(time)
    upload(bucketName, keyName, file)
    file.delete()
  }

  def write(file:File, json: JsonElement): Unit ={
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
