package service

import akka.actor.Actor
import com.google.inject.Inject
import domain.strategy.Strategies

class CandleActor @Inject()() extends Actor {

  def receive: PartialFunction[Any, Unit] = {
    case "1min" => Strategies.processEvery1minutes()
    case _      => println
  }
}
