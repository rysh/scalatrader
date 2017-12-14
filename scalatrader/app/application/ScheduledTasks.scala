package application

import javax.inject.{Named, Inject}

import akka.actor.{ActorRef, ActorSystem}
import domain.time.ScheduleHelper.initialDelay

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ScheduledTasks @Inject()(actorSystem: ActorSystem,
                               @Named("candle") candleActor: ActorRef,
                               @Named("positionAdjustment") positionAdjustment: ActorRef,
                               regularObservation: RegularObservation)(implicit executionContext: ExecutionContext) {
  def schedule(interval: FiniteDuration) = actorSystem.scheduler.schedule(initialDelay = initialDelay(interval), interval = interval)(_)

  if (!domain.isBackTesting) {
    schedule(1.minutes)(candleActor ! "1min")
    schedule(2.hours)(regularObservation summary)
    schedule(24.hours)(positionAdjustment ! "")

    // initial update
    positionAdjustment ! ""
  }
}