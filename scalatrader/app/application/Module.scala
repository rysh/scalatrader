package application

import com.google.inject.{Singleton, AbstractModule}
import domain.strategy.turtle.TurtleStrategy
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    bindActor[HelloActor]("hello")
    bindActor[CandleActor]("candle")

    bind(classOf[RegularObservation]).asEagerSingleton()
    bind(classOf[ExampleService]).asEagerSingleton()
    bind(classOf[RealTimeReceiver]).asEagerSingleton()
    bind(classOf[ScheduledTasks]).asEagerSingleton()
  }
}

@Singleton
class ExampleService {
  //Logger.debug("Hello World")
}