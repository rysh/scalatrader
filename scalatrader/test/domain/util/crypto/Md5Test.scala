package domain.util.crypto

import org.scalatest.FunSuite

class Md5Test extends FunSuite {

  test("testHex") {
    val hoge = Md5.hex("hoge")
    println(hoge)
  }

}
