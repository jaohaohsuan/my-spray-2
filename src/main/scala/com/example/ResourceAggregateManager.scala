package com.example

import akka.actor._
import java.util.{ UUID }
import akka.actor.SupervisorStrategy.{ Resume, Stop }

import scala.concurrent.duration._
import scala.language.postfixOps

object ResourceAggregateManagerProtocol {

  case class CreateResource(resource: AnyRef, resourcePath: String) extends AggregateManager.Command

}

object RootDirectory {

  import PermissionProtocol._
  import ResourceProtocol._

  def props = Props(classOf[ResourceAggregate], "7e5db785-d738-4679-8ae2-45b3b70cfe82")

  val path = "root"

  object `root` extends OwnerConfirmed("root", Set("root"))

}

class ResourceAggregateManager extends Actor with ActorLogging {

  context.setReceiveTimeout(2 seconds)

  import ResourceAggregateManagerProtocol._
  import ResourceProtocol._
  import PermissionProtocol._
  import RootDirectory._

  def receive = initial

  context.actorOf(props, path) ! Initialize

  val initial: Receive = {

    case GetUser =>
      sender ! `root`
    case _: Resource =>
      log.info("become established")
      context.become(established)
      context.setReceiveTimeout(Duration.Undefined)
    case ReceiveTimeout =>
      log.info("listen to '/root' timeout ")
      context.actorSelection(s"../${self.path.name}/root") ! GetState

    //context.child("root") match {
    //case Some(actorRef) =>
    //actorRef ! GetState
    //context.setReceiveTimeout(Duration.Undefined)
    //case None =>
    //   log.error("'/root has not been established")
    //}
  }

  val established: Receive = {

    case CreateResource(resource, resourcePath) =>
      context.actorSelection(s"../$path") forward resource

  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: InvalidActorNameException => {
        Resume
      }
    }

}
