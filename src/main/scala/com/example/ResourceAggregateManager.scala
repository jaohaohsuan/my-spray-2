package com.example

import akka.actor._
import java.util.{ UUID }
import akka.actor.SupervisorStrategy.{ Resume, Stop }
import com.example.PermissionProtocol.GetOwner

import scala.concurrent.duration._
import scala.language.postfixOps

object ResourceAggregateManagerProtocol {

  case class CreateResource(resource: AnyRef, resourcePath: String) extends AggregateManager.Command

}

object RootDirectory {

  import PermissionProtocol._
  import ResourceProtocol._

  def props = Props(classOf[ResourceAggregate], "7e5db785-d738-4679-8ae2-45b3b70cfe82")

  def `/root`(implicit self: ActorRef) = s"../${self.path.name}/root"

  object `root` extends OwnerConfirmed("root", Set("root"))

}

class ResourceAggregateManager extends Actor with ActorLogging {

  context.setReceiveTimeout(2 seconds)

  import ResourceAggregateManagerProtocol._
  import ResourceProtocol._
  import RootDirectory._
  import PermissionProtocol._

  def receive = initial

  context.actorOf(props, "root") ! Initialize(self)

  val initial: Receive = {
    case GetOwner =>
      sender ! `root`
    case _: Resource =>
      log.info("become established")
      context.become(established)

    case ReceiveTimeout =>
      log.info("listen to '/root' timeout ")
      context.actorSelection(`/root`) ! GetState
      context.setReceiveTimeout(Duration.Undefined)
  }

  val established: Receive = {

    case CreateResource(resource, resourcePath) =>
      context.actorSelection(`/root`) forward Handshaking(resource, None)

  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: InvalidActorNameException => {
        Resume
      }
    }

}
