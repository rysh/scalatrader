package domain.util.crypto

import org.apache.commons.codec.binary.Hex
import play.Play


object Aes {

  import java.security.Key
  import java.util.stream.IntStream
  import javax.crypto.Cipher
  import javax.crypto.spec.SecretKeySpec

  private val keyBits = 128
  /**
    * 秘密鍵を生成する
    *
    * @param seed 秘密鍵の元
    */
  def makeKey(seed: String): Key = {
    new SecretKeySpec(seed.getBytes().slice(0, keyBits / 8), "AES")
  }


  /**
    * 暗号化
    */
  def encode(src: String, key: String): String = try {
    if (src == null || src.length ==0) {
      src
    } else {
      Hex.encodeHexString(encode(src.getBytes(), Aes.makeKey(key)))
    }
  }
  def encode(src: Array[Byte], skey: Key): Array[Byte] = try {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, skey)
    cipher.doFinal(src)
  } catch {
    case e: Exception =>
      throw new RuntimeException(e)
  }

  /**
    * 復号化
    */

  def decode(src: String, key: String): String = try {
    if (src == null || src.length ==0) {
      src
    } else {
      new String(decode(Hex.decodeHex(src.toCharArray), Aes.makeKey(key)))
    }
  }
  def decode(src: Array[Byte], skey: Key): Array[Byte] = try {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, skey)
    cipher.doFinal(src)
  } catch {
    case e: Exception =>
      throw new RuntimeException(e)
  }

  def createKey(seed: String): Key = {
    val key = new Array[Byte](keyBits / 8)
    val bytesOfSeed = seed.getBytes
    IntStream.range(0, bytesOfSeed.length).filter((i: Int) => i < key.length).forEach((i: Int) => key(i) = bytesOfSeed(i))
    new SecretKeySpec(key, "AES")
  }
}
