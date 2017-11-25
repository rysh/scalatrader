package domain.time

import java.time.ZonedDateTime

object MockedTime {
  var now: ZonedDateTime = DateUtil.now()
}
