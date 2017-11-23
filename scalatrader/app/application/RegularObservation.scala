package application

import adapter.BitFlyer
import adapter.aws.{MailContent, SNS, SES}
import domain.models.{Position, Execution, Collateral, Positions}
import com.google.inject.Inject
import domain.time.DateUtil
import play.api.{Configuration, Logger}
import repository.UserRepository

class RegularObservation @Inject()(config: Configuration) {

  def summary(): Unit = {
    try {
      val str = config.get[String]("play.http.secret.key")
      println(str)
      summary(str)
    } catch {
      case e:Exception => {
        Logger.error("error in RegularObservation.summary", e)
      }
    }
  }

  def summary(secret: String): Unit = {
    val latest: Execution = BitFlyer.getLatestExecution()
    val users = UserRepository.all(secret)
    users.filter(user => notEmpty(user.api_key) && notEmpty(user.api_secret))
      .foreach(user => {
        try {
          val col: Collateral = BitFlyer.getCollateral(user.api_key, user.api_secret)
          val pos: Positions = BitFlyer.getPositions(user.api_key, user.api_secret)

          val content = createMailContent(user.email, latest, col, pos)
          SES.send(content)

        } catch {
          case e: Exception => {
            e.printStackTrace()
            Logger.error(s"RegularObservation.summary error: ${user.email}", e)
          }
        }
      })
  }

  def createMailContent(to: String, latest: Execution, col: Collateral, pos: Positions): MailContent = {
    val latestPrice = latest.price.toInt
    val delta = (pos.delta * latest.price).toInt
    val openPositionPnl = col.open_position_pnl.toInt
    val keepRate = (col.keep_rate * 100).toInt

    val subject = s"FX_BTC_JP ${DateUtil.jpDisplayTime} 定時観測"
    val html = htmlBody(latestPrice, delta, openPositionPnl, keepRate)
    val text = textBody(latestPrice, delta, openPositionPnl, keepRate)
    
    val from = "info@scalatrader.com"
    MailContent(to, from, subject, html, text)
  }

  def textBody(latestPrice:Int, delta: Int, openPositionPnl: Int, keepRate: Int) = {
    s"""
      |最終取引価格   ${"%,9d".format(latestPrice)}
      |デルタ         ${"%,9d".format(delta)}
      |評価損益       ${"%,9d".format(openPositionPnl)}
      |証拠金維持率   ${"%,9d".format(keepRate)} %
     """.stripMargin
  }

  def htmlBody(latestPrice:Int, delta: Int, openPositionPnl: Int, keepRate: Int) = {
    s"""<!DOCTYPE html>
        <html>
        <head>
          <title></title>
        </head>
        <body>
          <section>
            <table>
              <tbody style="text-align:right">
                <tr>
                  <th>最終取引価格</th>
                  <td>${"%,9d".format(latestPrice)}</td>
                </tr>
                <tr>
                  <th>デルタ</th>
                  <td>${"%,9d".format(delta)}</td>
                </tr>
                <tr>
                  <th>評価損益</th>
                  <td>${"%,9d".format(openPositionPnl)}</td>
                </tr>
                <tr>
                  <th>証拠金維持率</th>
                  <td>${"%,9d".format(keepRate)} %</td>
                </tr>
              </tbody>
            </table>
          </section>
        </body>
        </html>
      """.stripMargin
  }

  def notEmpty(str: String): Boolean = {
    if (str == null) {
      false
    } else {
      str.length > 0
    }
  }
}