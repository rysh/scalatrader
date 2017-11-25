package application

import com.google.inject.{Singleton, AbstractModule}
import domain.strategy.turtle.TurtleStrategy
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    println("Module.configure")
    domain.isBackTesting = true

    bindActor[CandleActor]("candle")

    if (domain.isBackTesting) {
      bind(classOf[BackTestApplication]).asEagerSingleton()
    } else {
      bind(classOf[RegularObservation]).asEagerSingleton()
      bind(classOf[RealTimeReceiver]).asEagerSingleton()
      bind(classOf[ScheduledTasks]).asEagerSingleton()
    }
  }
}