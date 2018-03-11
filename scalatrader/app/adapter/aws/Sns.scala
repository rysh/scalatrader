package adapter.aws

import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.{AmazonSNSClientBuilder, AmazonSNS}

object SNS {

  def send(email: String, message: String) = {

    val snsClient: AmazonSNS = AmazonSNSClientBuilder.standard().withRegion(Regions.US_EAST_2).build()

    val topicArn = "arn:aws:sns:us-east-2:855331286585:scalatrader-notifications"
    val msg = "My text published to SNS topic with email endpoint"
    val publishRequest = new PublishRequest(topicArn, message)

    val publishResult = snsClient.publish(publishRequest)
  }

}
