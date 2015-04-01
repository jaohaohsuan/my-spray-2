package com.example

import akka.actor._
import java.util.{ UUID }
import akka.actor.SupervisorStrategy.{ Resume, Stop }
import com.example.ResourceProtocol.{ CreatingResource, ResourceState }

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps

object ResourceAggregateManager {

  case class CreateResource(path: List[String], content: AnyRef) extends AggregateManager.Command

}

class ResourceAggregateManager extends Actor with ActorLogging {

  def receive = {

    case e: Any =>
      log.error(s"unexpected message '$e'")
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: InvalidActorNameException => {
        log.debug("InvalidActorNameException")
        Resume
      }
    }

}
