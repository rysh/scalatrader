package service

import java.time.ZonedDateTime
import javax.inject.Inject

import adapter.BitFlyer
import adapter.aws.{OrderQueueBody, SQS}
import play.api.{Configuration, Logger}
import repository.{RecordRepository, UserRepository}

import scala.concurrent.ExecutionContext

class ExecutionMonitorService @Inject()(config: Configuration, sqs: SQS)(implicit executionContext: ExecutionContext) {
  Logger.info("ExecutionMonitorService load")

  val secret: String = config.get[String]("play.http.secret.key")

  def run(): Unit = {
    sqs
      .list()
      .foreach(awsMessage => {
        val message: OrderQueueBody = sqs.map(awsMessage)
        UserRepository
          .get(message.email, secret)
          .foreach(user => {
            val executions = BitFlyer.getMyExecution(message.acceptanceId, user.api_key, user.api_secret)
            if (executions.nonEmpty) {
              try {
                application.retry(
                  3,
                  () => {
                    if (message.entryId.isEmpty) {
                      // entry
                      RecordRepository.insert(message.email, message.strategyStateId, message.acceptanceId, executions, ZonedDateTime.parse(message.timestamp))
                      sqs.deleteOrder(awsMessage)
                    } else {
                      // close
                      val count = RecordRepository.update(message.email, message.acceptanceId, message.entryId.get, executions, ZonedDateTime.parse(message.timestamp))
                      if (count > 0) {
                        sqs.deleteOrder(awsMessage)
                      }
                    }
                  }
                )
              } catch {
                case e: Exception => e.printStackTrace()
              }
            }
          })
      })
  }
}
