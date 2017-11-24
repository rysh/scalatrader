package application

import java.time.{ZonedDateTime, ZoneId}

import akka.actor.Actor
import domain.time.DateUtil


class HelloActor extends Actor {
  def receive = {
    case "tick" => println(DateUtil.now())
    case _ => println()
  }
}
