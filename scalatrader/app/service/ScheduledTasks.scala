package service

import javax.inject.{Named, Inject}

import akka.actor.{ActorRef, ActorSystem}
import application.RegularObservation
import domain.time.ScheduleHelper.initialDelay

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ScheduledTasks @Inject()(actorSystem: ActorSystem,
                               @Named("candle") candleActor: ActorRef,
                               @Named("positionAdjustment") positionAdjustment: ActorRef,
                               regularObservation: RegularObservation,
                               executionMonitorService: ExecutionMonitorService)(implicit executionContext: ExecutionContext) {
  def schedule(interval: FiniteDuration) = actorSystem.scheduler.schedule(initialDelay = initialDelay(interval), interval = interval)(_)

  if (!domain.isBackTesting) {
    schedule(1.minutes)(candleActor ! "1min")
    schedule(2.hours)(regularObservation summary ())
    schedule(24.hours)(positionAdjustment ! "")
    schedule(10.seconds)(executionMonitorService run ())

    // initial update
    positionAdjustment ! ""
  }
}
