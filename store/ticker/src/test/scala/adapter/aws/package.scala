package adapter

package object aws {

  def flake():String = {
    import java.time.format.DateTimeFormatter
    import java.time.{ZoneId, ZonedDateTime}
    ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("mm-ss"))
  }
}
