package domain.time

import java.time.format.DateTimeFormatter
import java.time.{ZonedDateTime, ZoneId}

object DateUtil {
  def format(time: ZonedDateTime, pattern: String): _root_.scala.Predef.String =
    time.format(DateTimeFormatter.ofPattern(pattern))

  def now() = ZonedDateTime.now(ZoneId.of("UTC"))

  def jpDisplayTime = ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))

  def keyOfUnit1Minutes(time: ZonedDateTime): Long = {
    time.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")).toLong
  }

  def keyOfUnit30Seconds(time: ZonedDateTime): Long = {
    val temp = time.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")).toLong
    temp - (temp % 30)
  }
}
