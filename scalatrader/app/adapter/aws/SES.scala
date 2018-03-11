package adapter.aws

import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model._

object SES {
  val client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_WEST_2).build()

  def send(content: MailContent): Unit = {
    val request = new SendEmailRequest()
      .withDestination(new Destination().withToAddresses(content.to))
      .withMessage(
        new Message()
          .withBody(new Body()
            .withHtml(new Content().withCharset("UTF-8").withData(content.htmlBody))
            .withText(new Content().withCharset("UTF-8").withData(content.textBody)))
          .withSubject(new Content().withCharset("UTF-8").withData(content.subject)))
      .withSource(content.from)
      .withConfigurationSetName("CONFIG")
    client.sendEmail(request)
  }
}

case class MailContent(to: String, from: String, subject: String, htmlBody: String, textBody: String)
