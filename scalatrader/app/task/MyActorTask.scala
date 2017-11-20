package tasks

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class MyActorTask @Inject() (actorSystem: ActorSystem, @Named("hello") someActor: ActorRef)(implicit executionContext: ExecutionContext) {

  actorSystem.scheduler.schedule(
    initialDelay = 0.microseconds,
    interval = 1.hours,
    receiver = someActor,
    message = "tick"
  )

}