package adapter.aws

import java.time.ZonedDateTime

import domain.time.DateUtil
import org.scalatest.FunSuite

class SQSTest extends FunSuite {

  ignore("testSend") {
    SQS.send(OrderQueueBody("hoge@scalatrader.com", "hoge1", DateUtil.now().toString))
    SQS.send(OrderQueueBody("hoge@scalatrader.com", "hoge2", DateUtil.now().toString))
    SQS.send(OrderQueueBody("hoge@scalatrader.com", "hoge3", DateUtil.now().toString))
  }

  ignore("testList") {
    val messages = SQS.list()
    println(messages.size)
    messages.map(message => {
      SQS.deleteOrder(message)

      val hoge = SQS.map(message)
      println(ZonedDateTime.parse(hoge.timestamp))
      hoge
    }).foreach(println)
  }
}
