package domain.time

import java.time.format.DateTimeFormatter
import java.time.{ZonedDateTime, ZoneId}

object DateUtil {

  def jpDisplayTime = ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
}
