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
import domain.strategy.turtle.{PriceReverseStrategy, TurtleMomentumStrategy, TurtleStrategy}
import domain.strategy.momentum.{MomentumReverseStrategy, MomentumStrategy}
import domain.time.{DateUtil, MockedTime}
import domain.time.DateUtil.format
import play.api.{Configuration, Logger}
import repository.UserRepository
import repository.model.scalatrader.User


@Singleton
class BackTestApplication @Inject()(config: Configuration, actorSystem: ActorSystem,
                                    @Named("candle") candleActor: ActorRef
                                   ) {
  Logger.info("init BackTestApplication")

  def run(start: ZonedDateTime, end: ZonedDateTime): Unit = {
    if (!domain.isBackTesting) return
    Logger.info("BackTestApplication run")
    BackTestResults.init()
    Strategies.init()
    Margin.resetSize()
    Logger.info("all init done")

    MockedTime.now = start

    val users: Seq[User] = UserRepository.everyoneWithApiKey(config.get[String]("play.http.secret.key"))
    if (users.isEmpty) return
    users.filter(user => !Strategies.values.exists(_.email == user.email))
//      .map(user => new TurtleStrategy(user))
//      .map(user => new TurtleMomentumStrategy(user))
//      .map(user => new MomentumStrategy(user))
      .map(user => new MomentumReverseStrategy(user))
      .foreach(st => {
      Strategies.register(st)
      st.availability.manualOn = true
    })

    val s3 = S3.create(Regions.US_WEST_1)
    Logger.info("initial data loading...")
    loadInitialData(s3)
    Logger.info("load data done")

    val gson: Gson = new Gson()
    while(MockedTime.now.isBefore(end)) {
      var ltp = 0.0

      val lines = DataLoader.fetchOrReadLines(s3, DateUtil.now)
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
                Logger.info(s"注文 ${order.side} time: ${DateUtil.format(time, "MM/dd HH:mm")}")
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
      Strategies.coreData.momentum5min.values.takeRight(1).foreach(t => BackTestResults.momentum.put(t._1,t._2))
      Strategies.coreData.hv30min.values.takeRight(1).foreach(t => BackTestResults.hv.put(t._1,t._2))
      Strategies.processEvery1minutes()
      //Logger.info(s"Margin(${BackTestResults.depositMargin}) ltp ($ltp)")
      //Margin.sizeUnit = new Margin(BackTestResults.depositMargin, Positions(Seq.empty[Position]), ltp).sizeOf1x
      MockedTime.now = MockedTime.now.plus(1, ChronoUnit.MINUTES)
      val now = DateUtil.now()
      val key = DateUtil.keyOf(now)
      Strategies.values.foreach(strategy => {
        if (!WaitingOrder.isWaiting(strategy.email, now)) {
          strategy.judgeEveryMinutes(key).map(Orders.market).foreach((order: models.Order) => {
            WaitingOrder.request(strategy.email, now, order)
            Logger.info(s"注文 ${order.side} time: ${DateUtil.format(now, "MM/dd HH:mm")}")
          })
        }
      })
    }
    BackTestResults.report()
  }

  private def loadInitialData(s3: S3): Unit = {
    val initialData: Seq[Ticker] = DataLoader.loadFromLocal()

    initialData.foreach(ticker => {
      Strategies.coreData.putTicker(ticker)
      Strategies.values.foreach(_.putTicker(ticker))
    })
    Strategies.processEvery1minutes()
    Strategies.values.foreach(st => st.availability.initialDataLoaded = true)
  }
}