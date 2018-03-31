package adapter.aws

import com.amazonaws.regions.Regions
import com.amazonaws.services.sqs.{AmazonSQSClientBuilder, AmazonSQS}
import com.amazonaws.services.sqs.model._
import javax.inject.Inject
import play.api.Configuration

import scala.collection.JavaConverters

class SQS @Inject()(config: Configuration) {
  import SQS.sqs

  val queueName: String = config.get[String]("aws.sqs.requestQueueName")

  def send(body: OrderQueueBody): Unit = {
    import io.circe.syntax._
    import io.circe.generic.auto._
    sqs.sendMessage(new SendMessageRequest(orderUrl(), body.asJson.toString()))
  }

  def orderUrl(): String = sqs.createQueue(new CreateQueueRequest(queueName)).getQueueUrl()

  def deleteOrder(message: Message): Unit = sqs.deleteMessage(new DeleteMessageRequest(orderUrl(), message.getReceiptHandle))

  def list(): Seq[Message] = {
    def fetch() = {
      val receiveMessageRequest = new ReceiveMessageRequest(orderUrl())
      receiveMessageRequest.setMaxNumberOfMessages(10)
      JavaConverters.collectionAsScalaIterable(sqs.receiveMessage(receiveMessageRequest).getMessages)
    }
    var result: List[Message] = List.empty
    var messages = fetch()
    while (messages.nonEmpty) {
      result = result ++: messages.toList
      messages = fetch()
    }
    result.toSeq
  }

  def map(message: Message): OrderQueueBody = {
    import io.circe.generic.auto._
    import io.circe.parser._
    decode[OrderQueueBody](message.getBody) match {
      case Right(ex) => ex
      case Left(err) => throw err
    }
  }
}

object SQS {
  val sqs: AmazonSQS = AmazonSQSClientBuilder.standard().withRegion(Regions.US_WEST_1).build()
}
case class OrderQueueBody(email: String, strategyStateId: Long, acceptanceId: String, timestamp: String, entryId: Option[String] = None)
