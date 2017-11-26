package application

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.{Named, Inject}

import adapter.aws.S3
import akka.actor.{ActorRef, ActorSystem}
import com.amazonaws.regions.Regions
import com.google.gson.Gson
import com.google.inject.Singleton
import domain.models.{Ticker, Orders, Position}
import domain.strategy.turtle.BackTestResults.OrderResult
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
//        val start = DateUtil.of("2017/11/20 17:13:00 +0000")
//    val end = DateUtil.of("2017/11/17 01:00:00 +0000")
//        val end = DateUtil.of("2017/11/20 17:16:00 +0000")

    //  val end = DateUtil.of("2017/11/25 16:00:00 +0000")
      val end = DateUtil.of("2017/11/26 14:00:00 +0000")
    MockedTime.now = start
    val s3 = S3.create(Regions.US_WEST_1)
    val gson: Gson = new Gson()

    lazy val users: Seq[User] = UserRepository.everyoneWithApiKey(secret)
    lazy val strategies = users.map(user => new TurtleStrategy(user))

    while(MockedTime.now.isBefore(end)){
      println(MockedTime.now)
      fetchOrReadLines(s3, MockedTime.now).foreach(json => {
        try {
          val ticker: Ticker = gson.fromJson(json, classOf[Ticker])

          strategies.foreach(strategy => {
            strategy.check(ticker.ltp).map(Orders.market).foreach(order => {
              BackTestResults.add(OrderResult(ticker.timestamp, order.side, ticker.ltp, order.size))
              println(s"注文 ${order.side} price: ${ticker.ltp} size:${order.size}")
              val positionToTuple: Position => (String, Double) = (pos: Position) => {
                val posSize: Double = pos.size * (if (pos.side == domain.Side.Sell) -1 else 1)
                val orderSize: Double = order.size * (if (order.side == domain.Side.Sell) -1 else 1)
                val total: Double = posSize + orderSize
                val side: String = if (total < 0) domain.Side.Sell else domain.Side.Buy
                (side, total.abs)
              }
              val maybePosition = TurtleCore.positionByUser.get(strategy.email)
              val (side: String, size: Double) = if (maybePosition.isEmpty) {
                (order.side, order.size)
              } else {
                positionToTuple(maybePosition.get)
              }
              if (size == 0) {
                TurtleCore.positionByUser.remove(strategy.email)
              } else {
                val position = Position(domain.ProductCode.btcFx,
                  side,
                  ticker.ltp,
                  size, Double.NaN, Double.NaN, Double.NaN, DateUtil.now.toString, Double.NaN, Double.NaN)
                TurtleCore.positionByUser.put(strategy.email, position)
              }
            })
          })
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