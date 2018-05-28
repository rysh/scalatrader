package service

import adapter.aws.SQS
import application._
import com.google.inject.AbstractModule
import play.api.Logger
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    Logger.info("Module.configure")
    domain.isBackTesting = true

    bindActor[CandleActor]("candle")
    bindActor[PositionSizeAdjustmentActor]("positionAdjustment")

    bind(classOf[SQS]).asEagerSingleton()
    bind(classOf[StrategySettingApplication]).asEagerSingleton()

    if (domain.isBackTesting) {
      bind(classOf[BackTestApplication]).asEagerSingleton()
    } else {
      bind(classOf[RegularObservation]).asEagerSingleton()
      bind(classOf[RealTimeReceiver]).asEagerSingleton()
      bind(classOf[BtcTickerReceiver]).asEagerSingleton()
      bind(classOf[ScheduledTasks]).asEagerSingleton()
      bind(classOf[InitializeService]).asEagerSingleton()
      bind(classOf[ExecutionMonitorService]).asEagerSingleton()
      bind(classOf[StrategyStateService]).asEagerSingleton()
    }
  }
}
