package object application {

  def retry[T](times: Int, func: () => T): T = {
    var i = 0
    var ret: Option[T] = None
    var waitTime = 0
    val delta = 5000
    while (i < times) {
      i += 1
      try {
        ret = Some(func())
        i = times
      } catch {
        case e: Exception => if (i == times) throw e
      }
      if (i < times) {
        waitTime = waitTime + delta
        Thread.sleep(waitTime)
      }
    }
    ret.get
  }
}
