package object application {

  def retry[T](times: Int, func: () => T): T = {
    var i = 0
    var ret: Option[T] = None
    while (i < times) {
      i += 1
      try {
        ret = Some(func())
        i = times
      } catch {
        case e: Exception => if (i == times) throw e
      }
      if (i < times) {
        Thread.sleep(2000)
      }
    }
    ret.get
  }
}
