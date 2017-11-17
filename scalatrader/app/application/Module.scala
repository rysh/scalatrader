package application


import akka.actor.Actor
import com.google.inject.{Singleton, AbstractModule}
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
  println("Hello World")
}

class HelloActor extends Actor {
  def receive = {
    case "tick" => println("HEllo")
    case _ => println
  }
}
