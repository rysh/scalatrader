package application

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.{Named, Inject}

import adapter.aws.S3
import akka.actor.{ActorRef, ActorSystem}
import com.amazonaws.regions.Regions
import com.google.gson.Gson
import com.google.inject.Singleton
import domain.backtest.{BackTestResults, WaitingOrder}
import domain.{Side, models}
import domain.models.{Position, Ticker, Orders}
import domain.backtest.BackTestResults.OrderResult
import domain.strategy.momentum.MomentumStrategy
import domain.strategy.{Strategies, Strategy}
import domain.strategy.turtle.{TurtleCore, TurtleStrategy}
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
    //  val start = DateUtil.of("2017/11/17 00:00:00 +0000")
    //  val start = DateUtil.of("2017/11/20 17:13:00 +0000")
    //  val end = DateUtil.of("2017/11/17 01:00:00 +0000")
    //  val end = DateUtil.of("2017/11/20 17:16:00 +0000")

    //  val end = DateUtil.of("2017/11/25 16:00:00 +0000")
    val start = DateUtil.of("2017/11/26 00:00:00 +0000")
    val end = DateUtil.of("2017/11/26 01:00:00 +0000")
//    run(start, end)
  } (scala.concurrent.ExecutionContext.Implicits.global)

  def run(start: ZonedDateTime, end: ZonedDateTime): Unit = {
    BackTestResults.init()

    val secret = config.get[String]("play.http.secret.key")

    MockedTime.now = start
    val s3 = S3.create(Regions.US_WEST_1)


    val users: Seq[User] = UserRepository.everyoneWithApiKey(secret)
    if (users.size == 0) return
    users.map(user => new MomentumStrategy(user)).foreach(Strategies.register)

    loadInitialData(s3)

    val gson: Gson = new Gson()
    while(MockedTime.now.isBefore(end)){
      fetchOrReadLines(s3, DateUtil.now).foreach(json => {
        try {
          val ticker: Ticker = gson.fromJson(json, classOf[Ticker])

          Strategies.values.foreach(strategy => {
            if (!WaitingOrder.isWaitingOrJustExecute(strategy.email, ticker, (order) => {
              BackTestResults.add(OrderResult(ticker.timestamp, order.side, ticker.ltp, order.size))

              val (side, size) = mergePosition(Strategies.getPosition(strategy.email), order)
              storePosition(ticker, strategy.email, side, size)
            })) {
              strategy.judge(ticker).map(Orders.market).foreach((order: models.Order) => {
                WaitingOrder.request(strategy.email, ticker, order)
                println(s"注文 ${order.side} time: ${ticker.timestamp}")
              })
            }
          })
          Strategies.putTicker(ticker)
          BackTestResults.addTicker(ticker)
        } catch {
          case e:Exception => e.printStackTrace()
        }
      })
      candleActor ! "1min"

      MockedTime.now = MockedTime.now.plus(1, ChronoUnit.MINUTES)
    }
    BackTestResults.report()
  }

  private def loadInitialData(s3: S3) = {
    val initialData = (1 to 20).reverse.map(i => {
      import DateUtil._
      val time = now().minus(i, ChronoUnit.MINUTES)
      (keyOfUnit1Minutes(time), fetchOrReadLines(s3, time))
    })
    Strategies.values.foreach(st => st.loadInitialData(initialData))
  }

  private def storePosition(ticker: Ticker, email: String, side: String, size: Double) = {
    if (size == 0) {
      Strategies.coreData.positionByUser.remove(email)
    } else {
      Strategies.coreData.positionByUser.put(email, positionOf(ticker, side, size))
    }
  }

  private def mergePosition(position: Option[Position], order: models.Order): (String, Double) = {
    position
      .map(_.relativeSize + order.relativeSize)
      .map(total => (Side.of(total), total.abs))
      .getOrElse((order.side, order.size))
  }

  private def positionOf(ticker: Ticker, side: String, size: Double) = {
    Position(domain.ProductCode.btcFx,
      side,
      ticker.ltp,
      size, Double.NaN, Double.NaN, Double.NaN, DateUtil.now.toString, Double.NaN, Double.NaN)
  }

  private def fetchOrReadLines(s3: S3, now: ZonedDateTime): Iterator[String] = {
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