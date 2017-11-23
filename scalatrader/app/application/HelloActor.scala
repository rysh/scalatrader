package application

import java.time.{ZoneId, ZonedDateTime}

import akka.actor.Actor


class HelloActor extends Actor {
  def receive = {
    case "tick" => println(ZonedDateTime.now(ZoneId.of("UTC")))
    case _ => println()
  }
}
