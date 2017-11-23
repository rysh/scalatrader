package adapter.aws

import org.scalatest.FunSuite

class SESTest extends FunSuite {

  test("testSend") {
    val content = MailContent(
      "rysh.cact@gmail.com",
      "info@scalatrader.com",
      "hoge",
      "<h1>hi</h1>",
      "hi",
    )
    //SES.send(content)
  }

}
