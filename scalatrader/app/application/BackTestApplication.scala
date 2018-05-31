package application

import java.time.ZonedDateTime

import javax.inject.{Inject, Named}
import adapter.aws.S3
import akka.actor.{ActorRef, ActorSystem}
import backtest.BackTestExecutor
import com.amazonaws.regions.Regions
import com.google.gson.Gson
import com.google.inject.Singleton
import domain.backtest.{BackTestResult, BackTestResults}
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

  lazy val executors = createStrategies("UpTrend4hStrategy") //"Dtn1mStrategy")

  def run(start: ZonedDateTime, end: ZonedDateTime): Unit = {
    if (!domain.isBackTesting || executors.isEmpty) return

    MockedTime.start(start)

    val s3 = S3.create(Regions.US_WEST_1)
    initialize(s3)

    val gson: Gson = new Gson()
    while (MockedTime.isFinished(end)) {

      DataLoader
        .fetchOrReadLines(s3, DateUtil.now())
        .foreach(json => {
          val ticker = gson.fromJson(json, classOf[Ticker])
          try {
            executors.foreach(_.execute(ticker))
          } catch {
            case e: Exception => e.printStackTrace()
          } finally {
            Strategies.putTicker(ticker)
            BackTestResults.addTicker(ticker)
          }
        })

      updateData()

      MockedTime.add1Minutes()
    }
    //BackTestResults.printSummary(executors)

    Logger.info("complete")
  }

  private def initialize(s3: S3): Unit = {
    Logger.info("BackTestApplication run")
    BackTestResults.init()
    Strategies.init()
    Margin.resetSize()
    Logger.info("all init done")
    Logger.info("initial data loading...")
    loadInitialData(s3)
    Logger.info("load data done")
  }

  private def updateData(): Unit = {
    Strategies.coreData.momentum5min.values.takeRight(1).foreach(t => BackTestResults.momentum.put(t._1, t._2))
    Strategies.coreData.hv30min.values.takeRight(1).foreach(t => BackTestResults.hv.put(t._1, t._2))
    Strategies.processEvery1minutes()
  }

  private def createStrategies(strategies: String*): Seq[BackTestExecutor] = {
    val dummyUser = User(0, "dummy@xxx.xxx", "", "dummy", "", "")
    def createStrategy(strategy: String): Strategy = {
      val available = true
      StrategyFactory.create(StrategyState(0L, strategy, available, 1.0), dummyUser)
    }
    strategies
      .map(createStrategy)
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
