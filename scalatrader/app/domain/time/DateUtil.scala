package domain.time

import java.time.format.DateTimeFormatter
import java.time._

object DateUtil {

  import DateTimeFormatter.ofPattern

  val zoneTokyo: ZoneId = ZoneId.of("Asia/Tokyo")
  val zoneUtc: ZoneId = ZoneId.of("UTC")

  def now(): ZonedDateTime = {
    if (domain.isBackTesting) {
      MockedTime.now
    } else {
      ZonedDateTime.now(zoneUtc)
    }
  }

  def format(time: ZonedDateTime, pattern: String): _root_.scala.Predef.String =
    time.format(ofPattern(pattern))

  def jpDisplayTime: String = {
    now().withZoneSameInstant(zoneTokyo).format(ofPattern("MM/dd HH:mm"))
  }

  def keyOf(time: ZonedDateTime, duration: Int = 1): Long = {
    val temp = time.format(ofPattern("yyyyMMddHHmmss")).toLong
    temp - (temp % 100 % duration)
  }
  def parseKey(key: Long): ZonedDateTime ={
    val local = LocalDateTime.parse(key.toString, ofPattern("yyyyMMddHHmmss"))
    ZonedDateTime.ofInstant(local.atOffset(ZoneOffset.ofHours(0)).toInstant, zoneUtc)
  }
  def fromTimestamp(str: String): ZonedDateTime = ZonedDateTime.parse(str, ofPattern("yyyy-MM-dd HH:mm:ss Z"))
  def of(str: String): ZonedDateTime = ZonedDateTime.parse(str, ofPattern("yyyy/MM/dd HH:mm:ss Z"))
  def keyToTimestamp(key: Long) = {
    parseKey(key).toOffsetDateTime.toString
  }
}
