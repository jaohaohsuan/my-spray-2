package com.example

import akka.actor._
import akka.persistence._
import java.util.{ UUID }
import akka.actor.SupervisorStrategy.{ Resume, Stop }

object ResourceProtocol {

  object Initialize extends Command

  case class QuerySyntax(name: String)

  case class ResourceCreated(name: String, groups: Set[String]) extends Event

  case class Resource(state: String, groups: Set[String]) extends State

}

object PermissionProtocol {

  object GetUser

  case class Handshaking(resource: AnyRef, user: Option[OwnerConfirmed])

  case class OwnerConfirmed(name: String, groups: Set[String])

}

class ResourceAggregate(uuid: String) extends PersistentActor with ActorLogging {

  override def persistenceId = uuid

  var state: State = Uninitialized

  import ResourceProtocol._
  import PermissionProtocol._

  val initial: Receive = {

    case Initialize =>
      sender ! GetUser
    case OwnerConfirmed(name, groups) =>
      persist(ResourceCreated(name, groups))(afterEventPersisted)
  }

  def receiveCommand = initial

  val idle: Receive = {
    //case Initialize =>
    //sender ! state
    case GetState =>
      sender ! state
    case Handshaking(q, Some(OwnerConfirmed(name, userGroups))) =>
      state match {
        case Resource(_, groups) if (groups & userGroups).size > 0 =>
          context.become(write)
          self ! q
      }
    case resource: QuerySyntax =>
      sender ! Handshaking(resource, None)

  }

  val write: Receive = {

    case QuerySyntax(name) =>
      context.actorOf(Props(classOf[ResourceAggregate], UUID.randomUUID.toString), name)
    case _ =>
      log.error("unsupported resource writing fail")

  }

  def afterEventPersisted(evt: Event): Unit = {
    updateState(evt)
    sender ! state
  }

  def updateState(evt: Event): Unit = evt match {
    case ResourceCreated(name, groups) =>
      state = Resource(name, groups)
      log.info("switch to idle")
      context.become(idle)
    case _ =>
  }

  val receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
    case SnapshotOffer(_, snapshot: State) =>
      state = snapshot

  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: InvalidActorNameException => {
        Resume
      }
      case ex: Exception =>
        log.error(ex, s"create child resource error: ${ex.getMessage}")
        Stop
    }
}
