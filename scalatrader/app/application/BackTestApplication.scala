package application

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import javax.inject.{Inject, Named}
import adapter.aws.S3
import akka.actor.{ActorRef, ActorSystem}
import backtest.BacktestExecutor
import com.amazonaws.regions.Regions
import com.google.gson.Gson
import com.google.inject.Singleton
import domain.backtest.{BackTestResults, WaitingOrder}
import domain.models
import domain.models.{Orders, Ticker}
import domain.backtest.BackTestResults.OrderResult
import domain.margin.Margin
import domain.strategy.{Strategies, Strategy, StrategyFactory, StrategyState}
import domain.time.{DateUtil, MockedTime}
import play.api.{Configuration, Logger}
import repository.UserRepository
import repository.model.scalatrader.User
import service.DataLoader
@Singleton
class BackTestApplication @Inject()(config: Configuration, actorSystem: ActorSystem, @Named("candle") candleActor: ActorRef) {
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
    val executors = users
      .filter(user => !Strategies.values.exists(_.email == user.email))
      .flatMap(user => {
        def createStrategy(strategy: String): Strategy = {
          StrategyFactory.create(StrategyState(0L, strategy, true, 1.0), user)
        }
        Seq(
          createStrategy(StrategyFactory.MixedBoxesStrategy),
          createStrategy(StrategyFactory.MixedBoxesStrategy)
        )
      })
      .map(st => {
        Strategies.register(st)
        new BacktestExecutor(st)
      })

    val s3 = S3.create(Regions.US_WEST_1)
    Logger.info("initial data loading...")
    loadInitialData(s3)
    Logger.info("load data done")

    val gson: Gson = new Gson()
    while (MockedTime.now.isBefore(end)) {

      DataLoader
        .fetchOrReadLines(s3, DateUtil.now())
        .foreach(json => executors.foreach(_.execute(gson.fromJson(json, classOf[Ticker]))))

      Strategies.coreData.momentum5min.values.takeRight(1).foreach(t => BackTestResults.momentum.put(t._1, t._2))
      Strategies.coreData.hv30min.values.takeRight(1).foreach(t => BackTestResults.hv.put(t._1, t._2))
      Strategies.processEvery1minutes()

      MockedTime.now = MockedTime.now.plus(1, ChronoUnit.MINUTES)
      val now = DateUtil.now()
      val key = DateUtil.keyOf(now)
      Strategies.values.foreach(strategy => {
        if (!WaitingOrder.isWaiting(strategy.email, now)) {
          strategy
            .judgeEveryMinutes(key)
            .map(Orders.market)
            .foreach((order: models.Order) => {
              WaitingOrder.request(strategy.email, now, order)
              //Logger.info(s"注文 ${order.side} time: ${DateUtil.format(now, "MM/dd HH:mm")}")
            })
        }
      })
    }
    BackTestResults.report()
    Logger.info("complete")
  }

  private def loadInitialData(s3: S3): Unit = {
    val initialData: Seq[Ticker] = DataLoader.loadFromLocal()

    initialData.foreach(ticker => {
      Strategies.coreData.putTicker(ticker)
      Strategies.values.foreach(_.putTicker(ticker))
    })
    Strategies.processEvery1minutes()
    DataLoader.loaded = true
  }
}
