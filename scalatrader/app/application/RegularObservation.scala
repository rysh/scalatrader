package application

import adapter.BitFlyer
import adapter.aws.{MailContent, SNS, SES}
import domain.models.{Position, Execution, Collateral, Positions}
import com.google.inject.Inject
import domain.margin.Margin
import domain.time.DateUtil
import play.api.{Configuration, Logger}
import repository.UserRepository

class RegularObservation @Inject()(config: Configuration) {
  val secret = config.get[String]("play.http.secret.key")

  def summary(): Unit = {
    try {
      summary(secret)
    } catch {
      case e:Exception => {
        Logger.error("error in RegularObservation.summary", e)
      }
    }
  }

  def summary(secret: String): Unit = {
    val latest: Execution = BitFlyer.getLatestExecution()
    UserRepository.everyoneWithApiKey(secret)
      .foreach(user => {
        try {
          val col: Collateral = BitFlyer.getCollateral(user.api_key, user.api_secret)
          val pos: Positions = BitFlyer.getPositions(user.api_key, user.api_secret)
          val lossCutLine = new Margin(col.collateral - col.open_position_pnl, pos, latest.price).lossCutLine

          val content = createMailContent(user.email, latest, col, pos, lossCutLine)
          SES.send(content)

        } catch {
          case e: Exception => {
            e.printStackTrace()
            Logger.error(s"RegularObservation.summary error: ${user.email}", e)
          }
        }
      })
  }

  def createMailContent(to: String, latest: Execution, col: Collateral, pos: Positions, lossCutLine: Option[Long]): MailContent = {
    val latestPrice = latest.price.toInt
    val delta = pos.btcFx.getOrElse(0.0).toInt
    val openPositionPnl = col.open_position_pnl.toInt
    val keepRate = (col.keep_rate * 100).toInt

    val subject = s"FX_BTC_JP ${DateUtil.jpDisplayTime} 定時観測"
    val html = htmlBody(latestPrice, delta, openPositionPnl, keepRate, lossCutLine)
    val text = textBody(latestPrice, delta, openPositionPnl, keepRate, lossCutLine)
    
    val from = "info@scalatrader.com"
    MailContent(to, from, subject, html, text)
  }

  def textBody(latestPrice:Int, delta: Int, openPositionPnl: Int, keepRate: Int, lossCutLine: Option[Long]) = {
    s"""
      |最終取引価格   ${"%,9d".format(latestPrice)}
      |デルタ         ${"%,9d".format(delta)}
      |評価損益       ${"%,9d".format(openPositionPnl)}
      |証拠金維持率   ${"%,9d".format(keepRate)} %
      |ロスカット水準  ${lossCutLine.map(e => "%,9d".format(e)).getOrElse("")}
     """.stripMargin
  }

  def htmlBody(latestPrice:Int, delta: Int, openPositionPnl: Int, keepRate: Int, lossCutLine: Option[Long]) = {
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
                <tr>
                  <th>ロスカット水準</th>
                  <td>${lossCutLine.map(e => "%,9d".format(e)).getOrElse("")}</td>
                </tr>
              </tbody>
            </table>
          </section>
        </body>
        </html>
      """.stripMargin
  }
}