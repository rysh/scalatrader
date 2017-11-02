package com.rysh.experiment.util

import java.time.Duration

object DateTimeUtil {

  def formatDuration(duration: Duration) = {
    import java.time.format.DateTimeFormatter
    import java.time.LocalTime
    val t = LocalTime.MIDNIGHT.plus(duration)
    DateTimeFormatter.ofPattern("HH:mm:ss").format(t)
  }
}
