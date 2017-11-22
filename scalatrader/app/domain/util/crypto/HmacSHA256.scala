package domain.util.crypto

import org.apache.commons.codec.binary.Hex

object HmacSHA256 {


  import javax.crypto.Mac
  import javax.crypto.spec.SecretKeySpec

  def encode(secret: String, dateStamp: String): String = {
    Hex.encodeHexString(HmacSHA256(dateStamp, secret.getBytes("UTF8")))
  }
  def HmacSHA256(data: String, key: Array[Byte]): Array[Byte] = {
    val algorithm = "HmacSHA256"
    val mac = Mac.getInstance(algorithm)
    mac.init(new SecretKeySpec(key, algorithm))
    mac.doFinal(data.getBytes("UTF8"))
  }
}
