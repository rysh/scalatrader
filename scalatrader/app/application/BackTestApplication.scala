package application

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import javax.inject.{Inject, Named}
import adapter.aws.S3
import akka.actor.{ActorRef, ActorSystem}
import backtest.BackTestExecutor
import com.amazonaws.regions.Regions
import com.google.gson.Gson
import com.google.inject.Singleton
import domain.backtest.BackTestResults
import domain.models.Ticker
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
    val executors = createStrategies(users, StrategyFactory.MixedBoxesStrategy, StrategyFactory.MixedBoxesStrategy)

    val s3 = S3.create(Regions.US_WEST_1)
    Logger.info("initial data loading...")
    loadInitialData(s3)
    Logger.info("load data done")

    val gson: Gson = new Gson()
    while (MockedTime.now.isBefore(end)) {

      DataLoader
        .fetchOrReadLines(s3, DateUtil.now())
        .foreach(json => executors.foreach(_.execute(gson.fromJson(json, classOf[Ticker]))))

      updateData()

      MockedTime.now = MockedTime.now.plus(1, ChronoUnit.MINUTES)
    }
    BackTestResults.report()
    Logger.info("complete")
  }

  private def updateData(): Unit = {
    Strategies.coreData.momentum5min.values.takeRight(1).foreach(t => BackTestResults.momentum.put(t._1, t._2))
    Strategies.coreData.hv30min.values.takeRight(1).foreach(t => BackTestResults.hv.put(t._1, t._2))
    Strategies.processEvery1minutes()
  }

  private def createStrategies(users: Seq[User], strategies: String*): Seq[BackTestExecutor] = {
    users
      .filter(user => !Strategies.values.exists(_.email == user.email))
      .flatMap(user => {
        def createStrategy(strategy: String): Strategy = {
          val available = true
          StrategyFactory.create(StrategyState(0L, strategy, available, 1.0), user)
        }
        strategies.map(createStrategy)
      })
      .map(st => {
        Strategies.register(st)
        new BackTestExecutor(st)
      })
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
