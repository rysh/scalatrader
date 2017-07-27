package domain

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import better.files.File

/**
  * Created by ryuhei.ishibashi on 2017/07/13.
  */
object TickerFile {
  def fileOfTime(): File = {
    import better.files._
    File(fileNameFromNow(ZonedDateTime.now(ZoneId.of("UTC")))).createIfNotExists()
  }

  def fileNameFromNow(now: ZonedDateTime):String = {
    now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd/hh/mm"))
  }
}
