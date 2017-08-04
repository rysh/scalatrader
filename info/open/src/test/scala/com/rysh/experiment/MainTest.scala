package com.rysh.experiment

import org.scalatest.FunSuite

class MainTest extends FunSuite {

  test("testMain") {
    try {
      Main.main(Array())
      assert(true)
    } catch {case e :Exception => fail(e)}
  }
}
