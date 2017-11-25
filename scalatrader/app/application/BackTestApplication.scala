package application

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.{Named, Inject}

import adapter.aws.S3
import akka.actor.{ActorRef, ActorSystem}
import com.amazonaws.regions.Regions
import com.google.gson.Gson
import com.google.inject.Singleton
import domain.models.Ticker
import domain.strategy.turtle.{BackTestResults, TurtleCore, TurtleStrategy}
import domain.time.{DateUtil, MockedTime}
import domain.time.DateUtil.format
import play.api.Configuration
import repository.UserRepository
import repository.model.scalatrader.User

import scala.concurrent.Future

@Singleton
class BackTestApplication @Inject()(config: Configuration, actorSystem: ActorSystem,
                                    @Named("candle") candleActor: ActorRef
                                   ) {
  println("init BackTestApplication")
  
  Future {
    run()
  } (scala.concurrent.ExecutionContext.Implicits.global)

  def run() = {

    val secret = config.get[String]("play.http.secret.key")

    val start = DateUtil.of("2017/11/17 00:00:00 +0000")
    val end = DateUtil.of("2017/11/17 01:00:00 +0000")
    //  val end = DateUtil.of("2017/11/25 16:00:00 +0000")
    MockedTime.now = start
    val s3 = S3.create(Regions.US_WEST_1)
    val gson: Gson = new Gson()

    lazy val users: Seq[User] = UserRepository.everyoneWithApiKey(secret)
    lazy val turtleStrategy = new TurtleStrategy(users, candleActor)

    while(MockedTime.now.isBefore(end)){
      fetchOrReadLines(s3, MockedTime.now).foreach(json => {
        try {
          val ticker: Ticker = gson.fromJson(json, classOf[Ticker])

          turtleStrategy.exec(ticker.ltp)
          TurtleCore.put(ticker)
        } catch {
          case e:Exception => e.printStackTrace()
        }
      })
      candleActor ! "1min"

      //TODO 内部的にポジションを保持する
      //TODO 内部的に結果を保持しdashboadに出力する

      MockedTime.now = MockedTime.now.plus(1, ChronoUnit.MINUTES)
    }

    BackTestResults.report()
  }

  private def fetchOrReadLines(s3: S3, now: ZonedDateTime) = {
    val localPath = "tmp/btc_fx/"

    val filePath = localPath + format(now, "yyyyMMddHHmm")
    val file = better.files.File(filePath)
    if (file.isEmpty) {
      file.createIfNotExists()
      try {
        val lines = s3.getLines("btcfx-ticker-scala", format(now, "yyyy/MM/dd/HH/mm"))
        lines.withFilter(l => l.length > 0).foreach(line => file.appendLine(line))
        lines
      } catch {
        case e:Exception => Iterator.empty
      }
    } else {
      file.lines.filter(l => l.length > 0).toIterator
    }
  }
}