package application


import akka.actor.Actor
import com.google.inject.{Singleton, AbstractModule}
import play.api.Logger
import play.api.libs.concurrent.AkkaGuiceSupport
import tasks.MyActorTask

class TasksModule extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    bind(classOf[ExampleService]).asEagerSingleton()
    bindActor[HelloActor]("hello")
    bind(classOf[MyActorTask]).asEagerSingleton()
  }
}

@Singleton
class ExampleService {
  Logger.debug("Hello World")
}

class HelloActor extends Actor {
  def receive = {
    case "tick" => Logger.debug("Hello")
    case _ => println
  }
}
