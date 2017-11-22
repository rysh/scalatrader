package adapter.aws

import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.{Destination, Message, Body, Content}

object SES {

  def send(email:String, message:String): Unit = {
//val snsClient: AmazonSNS = AmazonSNSClientBuilder.standard().withRegion(Regions.US_WEST_1).build()
    val client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_WEST_2).build();
    import com.amazonaws.services.simpleemail.model.SendEmailRequest
    val HTMLBODY = "<h1>hi</h1>"
    val TEXTBODY = "hi"
    val SUBJECT = "hoge"
    val FROM = "rysh.cact@gmmail.com"
    val CONFIGSET = "CONFIG"
    val request = new SendEmailRequest()
        .withDestination(new Destination().withToAddresses("rysh.cact@gmmail.com"))
        .withMessage(new Message()
          .withBody(new Body()
            .withHtml(new Content().withCharset("UTF-8").withData(HTMLBODY))
            .withText(new Content().withCharset("UTF-8").withData(TEXTBODY)))
          .withSubject(new Content().withCharset("UTF-8").withData(SUBJECT)))
      .withSource(FROM)
      .withConfigurationSetName(CONFIGSET)
    client.sendEmail(request)
    System.out.println("Email sent!")
  }

}
