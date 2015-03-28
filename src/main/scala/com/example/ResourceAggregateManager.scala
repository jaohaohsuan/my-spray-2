package com.example

import akka.actor._
import java.util.{ UUID }
import akka.actor.SupervisorStrategy.{ Resume, Stop }
import com.example.ResourceProtocol.{CreatingResource, ResourceState}


import scala.concurrent.duration._
import scala.language.postfixOps

import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps

object ResourceAggregateManager {

  case class CreateResource(path: List[String], content: AnyRef) extends AggregateManager.Command

}

class ResourceAggregateManager extends Actor with ActorLogging {

  import ResourceAggregateManager._
  import ResourceProtocol._


  val rootProps = Props(classOf[ResourceAggregate], "7e5db785-d738-4679-8ae2-45b3b70cfe82")

  context.actorOf(rootProps, "root") ! GetState

  def receive = {
    case Uninitialized =>
      sender ! CreatingResource("root" :: Nil, null, Some("admin"), Some(Set("admin")))

    case _: ResourceState =>
      context.become(rootEstablished)
      log.info("root is rootEstablished")
    case m: AnyRef =>
      log.error(s"unexpected message '$m'")
  }

  val rootEstablished: Receive = {

    case CreateResource(path, content) =>
      sender ! CreatingResource(path, content, None, None)

    case c: CreatingResource =>
      context.actorSelection(s"../${self.path.name}/root") forward c
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: InvalidActorNameException => {
        log.debug("InvalidActorNameException")
        Resume
      }
    }

}
