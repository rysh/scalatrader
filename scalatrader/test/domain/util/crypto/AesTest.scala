package domain.util.crypto

import org.apache.commons.codec.binary.Hex
import org.scalatest.FunSuite

class AesTest extends FunSuite {

  test("testEncode") {
    val skey = "QCYtAnfkaZiwrNwnxIlR6CTfG3gf90Latabg5241ABR5W1uDFNIkn"

    val input = "+q8minmDbMyAzL7oeycL4kS5sRUMxTNKRnb7ugHJhjs="
    // 暗号化
    val enc = Aes.encode(input, skey)

    // 復号化
    val dec = Aes.decode(enc, skey)

    assert(dec === input)
  }

}
