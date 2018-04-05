package adapter.aws

import java.time.ZonedDateTime

import com.typesafe.config.ConfigFactory
import domain.time.DateUtil
import org.scalatest.FunSuite
import play.api.Configuration

class SQSTest extends FunSuite {

  ignore("testSend") {
    val sqs = new SQS(new Configuration(ConfigFactory.load()))
    sqs.send(OrderQueueBody("hoge@scalatrader.com", 1L, "hoge1", DateUtil.now().toString))
    sqs.send(OrderQueueBody("hoge@scalatrader.com", 2L, "hoge2", DateUtil.now().toString))
    sqs.send(OrderQueueBody("hoge@scalatrader.com", 3L, "hoge3", DateUtil.now().toString))
  }

  ignore("testList") {
    val sqs = new SQS(new Configuration(ConfigFactory.load()))
    val messages = sqs.list()
    println(messages.size)
    messages
      .map(message => {
        sqs.deleteOrder(message)

        val hoge = sqs.map(message)
        println(ZonedDateTime.parse(hoge.timestamp))
        hoge
      })
      .foreach(println)
  }
}
