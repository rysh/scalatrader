
import akka.actor.Actor
import com.google.inject.{Singleton, AbstractModule}

class TasksModule extends AbstractModule {

  override def configure() = {
    bind(classOf[ExampleService]).asEagerSingleton()
  }
}

@Singleton
class ExampleService {
  println("Hello World")
}
