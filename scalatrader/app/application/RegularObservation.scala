package application

import adapter.BitFlyer
import adapter.aws.SNS
import adapter.bitflyer.models.{Position, Execution, Collateral}
import com.google.inject.Inject
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
          val pos: Seq[Position] = BitFlyer.getPositions(user.api_key, user.api_secret)

          val message = createMessage(latest, col, pos)
          println(message)
          //TODO SES
          SNS.send(user.email, message)

        } catch {
          case e: Exception => {
            e.printStackTrace()
            Logger.error(s"RegularObservation.summary error: ${user.email}", e)
          }
        }
      })
  }

  def createMessage(latest: Execution, col: Collateral, pos: Seq[Position]):String = {
    val delta = pos.map(p => if (p.side == "SELL") (- p.size) else p.size ).sum
    s"""
       |最終取引価格   ${"%,9d".format(latest.price.toInt)}
       |デルタ         ${"%,9d".format((delta * latest.price).toInt)}
       |評価損益       ${"%,9d".format(col.open_position_pnl.toInt)}
       |証拠金維持率   ${"%,9d".format((col.keep_rate * 100).toInt)} %
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