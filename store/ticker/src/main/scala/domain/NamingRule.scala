package domain

class NamingRule(bucketName: String) {
  def dir: String = bucketName
  def path(time: TimeKeeper): String = "%s/%s".format(dir, time.format("yyyyMMddHHmm"))
  def s3Path(time: TimeKeeper): String = time.format("yyyy/MM/dd/HH/mm")
}
