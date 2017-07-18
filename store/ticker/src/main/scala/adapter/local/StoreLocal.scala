package adapter.local

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import adapter.bitflyer.realtime.Store
import better.files.File
import com.google.gson.JsonElement

/**
  * Created by ryuhei.ishibashi on 2017/07/06.
  */
class StoreLocal(bucketName: String) extends Store {
  val file = fileOfTime()
  val time = ZonedDateTime.now(ZoneId.of("UTC"))

  override def keep(json: JsonElement): Unit = {
    write(file, json)
  }

  override def store(): Either[Unit, Store] = {
    if (???) {
      return Left()
    }
    println("no upload and no delete")
    Right(new StoreLocal(bucketName))
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
