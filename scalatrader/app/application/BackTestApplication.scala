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
import domain.models.{Position, Ticker, Orders, Positions}
import domain.backtest.BackTestResults.OrderResult
import domain.margin.Margin
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
    Strategies.init()
    Margin.resetSize()

    MockedTime.now = start

    val users: Seq[User] = UserRepository.everyoneWithApiKey(config.get[String]("play.http.secret.key"))
    if (users.isEmpty) return
    users.map(user => new TurtleStrategy(user)).foreach(st => {
      if (!Strategies.values.exists(_.email == st.email)) {
        Strategies.register(st)
      }
    })

    val s3 = S3.create(Regions.US_WEST_1)
    loadInitialData(s3)

    val gson: Gson = new Gson()
    while(MockedTime.now.isBefore(end)) {
      var ltp = 0.0

      val lines = fetchOrReadLines(s3, DateUtil.now)
      lines.foreach(json => {
        val ticker: Ticker = gson.fromJson(json, classOf[Ticker])
        try {
          val time = ZonedDateTime.parse(ticker.timestamp)
          Strategies.values.foreach(strategy => {
            if (!WaitingOrder.isWaitingOrJustExecute(strategy.email, time, (order) => {
              BackTestResults.add(OrderResult(ticker.timestamp, order.side, ticker.ltp, order.size))
            })) {
              strategy.judgeByTicker(ticker).map(Orders.market).foreach((order: models.Order) => {
                WaitingOrder.request(strategy.email, time, order)
                println(s"注文 ${order.side} time: ${DateUtil.format(time, "MM/dd HH:mm")}")
              })
            }
          })
        } catch {
          case e:Exception => throw e
        } finally {
          Strategies.putTicker(ticker)
          BackTestResults.addTicker(ticker)
          ltp = ticker.ltp
        }
      })
      Strategies.processEvery1minutes()
      println(s"Margin(${BackTestResults.depositMargin}) ltp ($ltp)")
      Margin.sizeUnit = new Margin(BackTestResults.depositMargin, Positions(Seq.empty[Position]), ltp).sizeOf1x
      MockedTime.now = MockedTime.now.plus(1, ChronoUnit.MINUTES)
      val now = DateUtil.now()
      val key = DateUtil.keyOfUnit1Minutes(now)
      Strategies.values.foreach(strategy => {
        if (!WaitingOrder.isWaiting(strategy.email, now)) {
          strategy.judgeEveryMinutes(key).map(Orders.market).foreach((order: models.Order) => {
            WaitingOrder.request(strategy.email, now, order)
            println(s"注文 ${order.side} time: ${DateUtil.format(now, "MM/dd HH:mm")}")
          })
        }
      })
    }
    BackTestResults.report()
  }

  private def loadInitialData(s3: S3): Unit = {
    val gson: Gson = new Gson
    val initialData: Seq[Ticker] = (1 to 20).reverse.flatMap(i => {
      import DateUtil._
      val time = now().minus(i, ChronoUnit.MINUTES)
      fetchOrReadLines(s3, time)
    }).map(json => gson.fromJson(json, classOf[Ticker]))

    initialData.foreach(ticker => {
      Strategies.coreData.putTicker(ticker)
      Strategies.values.foreach(_.putTicker(ticker))
    })
    Strategies.values.foreach(st => st.isAvailable = true)
  }

  private def fetchOrReadLines(s3: S3, now: ZonedDateTime): Iterator[String] = {
    val localPath = "tmp/btc_fx/"

    val filePath = localPath + format(now, "yyyyMMddHHmm")
    val file = better.files.File(filePath)
    if (file.isEmpty) {
      file.createIfNotExists()
      try {
        val lines = s3.getLines("btcfx-ticker-scala", format(now, "yyyy/MM/dd/HH/mm")).toSeq
        lines.withFilter(l => l.length > 0).foreach(line => file.appendLine(line))
        lines.toIterator
      } catch {
        case e:Exception => Iterator.empty
      }
    } else {
      file.lines.filter(l => l.length > 0).toIterator
    }
  }
}