package application

import com.google.inject.{Singleton, AbstractModule}
import play.api.Logger
import play.api.libs.concurrent.AkkaGuiceSupport
import repository.UserRepository

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    bindActor[HelloActor]("hello")

    bind(classOf[RegularObservation]).asEagerSingleton()
    bind(classOf[ExampleService]).asEagerSingleton()
    bind(classOf[ScheduledTasks]).asEagerSingleton()
  }
}

@Singleton
class ExampleService {
  Logger.debug("Hello World")
}