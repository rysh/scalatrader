package domain

object NamingRule {
  def dir: String = "tmp"
  def path(time: TimeKeeper): String = "%s/%s-%s".format(dir, "ticker", time.format("yyyyMMddHHmm"))
  def s3Path(time: TimeKeeper): String = time.format("yyyy/MM/dd/hh/mm");
}
