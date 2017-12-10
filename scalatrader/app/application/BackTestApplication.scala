package application

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.{Named, Inject}

import adapter.aws.S3
import akka.actor.{ActorRef, ActorSystem}
import com.amazonaws.regions.Regions
import com.google.gson.Gson
import com.google.inject.Singleton
import domain.backtest.{BackTestResults, WaitingOrder}
import domain.models
import domain.models.{Ticker, Orders}
import domain.backtest.BackTestResults.OrderResult
import domain.margin.Margin
import domain.strategy.Strategies
import domain.strategy.turtle.{PriceReverseStrategy, TurtleStrategy, TurtleMomentumStrategy}
import domain.time.{DateUtil, MockedTime}
import domain.time.DateUtil.format
import play.api.Configuration
import repository.UserRepository
import repository.model.scalatrader.User


@Singleton
class BackTestApplication @Inject()(config: Configuration, actorSystem: ActorSystem,
                                    @Named("candle") candleActor: ActorRef
                                   ) {
  println("init BackTestApplication")

  def run(start: ZonedDateTime, end: ZonedDateTime): Unit = {
    if (!domain.isBackTesting) return
    println("BackTestApplication run")
    BackTestResults.init()
    Strategies.init()
    Margin.resetSize()

    MockedTime.now = start

    val users: Seq[User] = UserRepository.everyoneWithApiKey(config.get[String]("play.http.secret.key"))
    if (users.isEmpty) return
    users.filter(user => !Strategies.values.exists(_.email == user.email))
//      .map(user => new TurtleStrategy(user))
      .map(user => new TurtleMomentumStrategy(user))
      .foreach(st => {
      Strategies.register(st)
      st.availability.manualOn = true
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
      Strategies.coreData.momentum20.values.takeRight(3).foreach(t => BackTestResults.momentum.put(t._1,t._2))
      Strategies.processEvery1minutes()
      //println(s"Margin(${BackTestResults.depositMargin}) ltp ($ltp)")
      //Margin.sizeUnit = new Margin(BackTestResults.depositMargin, Positions(Seq.empty[Position]), ltp).sizeOf1x
      MockedTime.now = MockedTime.now.plus(1, ChronoUnit.MINUTES)
      val now = DateUtil.now()
      val key = DateUtil.keyOf(now)
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
    val initialData: Seq[Ticker] = (1 to 60).reverse.flatMap(i => {
      import DateUtil._
      val time = now().minus(i, ChronoUnit.MINUTES)
      fetchOrReadLines(s3, time)
    }).map(json => gson.fromJson(json, classOf[Ticker]))

    initialData.foreach(ticker => {
      Strategies.coreData.putTicker(ticker)
      Strategies.values.foreach(_.putTicker(ticker))
    })
    Strategies.coreData.momentum10.loadAll()
    Strategies.processEvery1minutes()
    Strategies.values.foreach(st => st.availability.initialDataLoaded = true)
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