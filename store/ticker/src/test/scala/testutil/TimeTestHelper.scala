package testutil

import java.time.{ZoneId, ZonedDateTime}

object TimeTestHelper {
  def of(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int, second: Int, nano: Int): ZonedDateTime = ZonedDateTime.of(year,month,dayOfMonth,hour,minute,second, nano, ZoneId.of("UTC"))
}
