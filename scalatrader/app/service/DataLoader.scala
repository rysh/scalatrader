package service

import domain.time.DateUtil.{format, now}
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import adapter.aws.S3
import com.amazonaws.regions.Regions
import com.google.gson.Gson
import domain.models
import domain.models.Ticker
import play.api.Logger

object DataLoader {

  var loaded = false

  def loadFromS3(): Seq[Ticker] = {
    val s3 = S3.create(Regions.US_WEST_1)
    val gson: Gson = new Gson
    val refTime = now()
    (1 to 120).reverse.flatMap(i => {
      try {
        val time = refTime.minus(i, ChronoUnit.MINUTES)
        val s3Path: String = format(time, "yyyy/MM/dd/HH/mm")
        Logger.info(s"loading... $s3Path")
        s3.getLines("btcfx-ticker-scala", s3Path)
      } catch {
        case _:Exception => Iterator.empty
      }
    }).map(json => gson.fromJson(json, classOf[Ticker]))
  }

  def loadFromLocal(): Seq[models.Ticker] = {
    val s3 = S3.create(Regions.US_WEST_1)

    val refTime = now()
    val gson: Gson = new Gson
    (1 to 120).reverse.flatMap(i => {
      val time = refTime.minus(i, ChronoUnit.MINUTES)
      fetchOrReadLines(s3, time)
    }).map(json => gson.fromJson(json, classOf[Ticker]))
  }


  def fetchOrReadLines(s3: S3, now: ZonedDateTime): Iterator[String] = {
    val localPath = "tmp/btc_fx/"

    try {
    val filePath = localPath + format(now, "yyyyMMddHHmm")
    val file = better.files.File(filePath)
      if (file.isEmpty) {
        file.createIfNotExists()
          val path = format(now, "yyyy/MM/dd/HH/mm")
          Logger.info(s"loading... $path")
          val lines = s3.getLines("btcfx-ticker-scala", path).toSeq
          lines.withFilter(l => l.length > 0).foreach(line => file.appendLine(line))
          lines.toIterator
      } else {
        file.lines.filter(l => l.length > 0).toIterator
      }
    } catch {
      case _:Exception => Iterator.empty
    }
  }
}
