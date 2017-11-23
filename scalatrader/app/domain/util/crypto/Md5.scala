package domain.util.crypto

object Md5 {
  def hex(str: String) = {
    import org.apache.commons.codec.digest.DigestUtils
    DigestUtils.md5Hex(str)
  }

}
