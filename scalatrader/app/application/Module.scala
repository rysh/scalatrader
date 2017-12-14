package application

import com.google.inject.{Singleton, AbstractModule}
import domain.strategy.turtle.TurtleStrategy
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    println("Module.configure")
    domain.isBackTesting = false

    bindActor[CandleActor]("candle")
    bindActor[PositionSizeAdjustmentActor]("positionAdjustment")

    if (domain.isBackTesting) {
      bind(classOf[BackTestApplication]).asEagerSingleton()
    } else {
      bind(classOf[RegularObservation]).asEagerSingleton()
      bind(classOf[RealTimeReceiver]).asEagerSingleton()
      bind(classOf[ScheduledTasks]).asEagerSingleton()
      bind(classOf[InitializeService]).asEagerSingleton()
    }
  }
}