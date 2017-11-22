package application

import javax.inject.{Named, Inject}

import akka.actor.{ActorRef, ActorSystem}
import domain.time.ScheduleHelper.initialDelay

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ScheduledTasks @Inject()(actorSystem: ActorSystem,
                               @Named("hello") someActor: ActorRef,
                               regularObservation: RegularObservation)(implicit executionContext: ExecutionContext) {
  def schedule(interval: FiniteDuration) = actorSystem.scheduler.schedule(initialDelay = initialDelay(interval), interval = interval)(_)

  schedule(30.minutes)(someActor ! "tick")
  schedule(2.hours)(regularObservation summary)
}