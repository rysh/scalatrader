package domain.time

import java.time.format.DateTimeFormatter
import java.time.{ZonedDateTime, ZoneId}

object DateUtil {

  import DateTimeFormatter.ofPattern

  val zoneTokyo = ZoneId.of("Asia/Tokyo")
  val zoneUtc = ZoneId.of("UTC")

  def now() = {
    if (domain.isBackTesting) {
      MockedTime.now
    } else {
      ZonedDateTime.now(zoneUtc)
    }
  }

  def format(time: ZonedDateTime, pattern: String): _root_.scala.Predef.String =
    time.format(ofPattern(pattern))

  def jpDisplayTime = {
    now().withZoneSameInstant(zoneTokyo).format(ofPattern("MM/dd HH:mm"))
  }

  def keyOfUnit1Minutes(time: ZonedDateTime): Long = {
    time.format(ofPattern("yyyyMMddHHmm")).toLong
  }

  def keyOfUnitSeconds(time: ZonedDateTime, duration: Int): Long = {
    val temp = time.format(ofPattern("yyyyMMddHHmmss")).toLong
    temp - (temp % 100 % duration)
  }

  def fromTimestamp(str: String) = ZonedDateTime.parse(str, ofPattern("yyyy-MM-dd HH:mm:ss Z"))
  def of(str: String) = ZonedDateTime.parse(str, ofPattern("yyyy/MM/dd HH:mm:ss Z"))
}
