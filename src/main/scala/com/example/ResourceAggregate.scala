package com.example

import akka.actor._
import akka.persistence._
import java.util.{ UUID }
import akka.actor.SupervisorStrategy.{ Resume, Stop }

object ResourceProtocol {

  case class Initialize(pipe: ActorRef) extends Command

  case class Touch(resource: AnyRef, owner: ActorRef)

  case class QuerySyntax(name: String, syntax: String)

  case class ResourceCreated(name: String, groups: Set[String]) extends Event

  case class Resource(state: String, groups: Set[String]) extends State

}

object PermissionProtocol {

  case class Handshaking(resource: AnyRef, user: Option[OwnerConfirmed])

  case class OwnerConfirmed(name: String, groups: Set[String])

}

class ResourceAggregate(uuid: String) extends PersistentActor with ActorLogging {

  override def persistenceId = uuid

  var state: State = Uninitialized

  import ResourceProtocol._
  import PermissionProtocol._

  def receiveCommand = initial

  val initial: Receive = {

    case Initialize(owner) =>
      log.info(s"ask to get owner ${owner}")
      owner ! "GetOwner"
    case OwnerConfirmed(name, groups) =>
      persist(ResourceCreated(name, groups))(afterEventPersisted)
  }

  val idle: Receive = {
    case GetState =>
      sender ! state
    case msg @ Handshaking(_, None) =>
      sender ! msg
    case Handshaking(resource, Some(OwnerConfirmed(name, userGroups))) =>
      state match {
        case Resource(_, groups) if (groups & userGroups).size > 0 =>
          context.become(write)
          log.info("switch to write")
          self ! Touch(resource, sender)
      }
  }

  val write: Receive = {

    case Handshaking(resource, _) =>
      self ! Touch(resource, sender)
    case Touch(QuerySyntax(name, _), owner) =>
      context.actorOf(Props(classOf[ResourceAggregate], UUID.randomUUID.toString), name) ! Initialize(owner)
      log.info(s"QuerySyntax resource '$name' is created")
    case _ =>
      context.become(idle)
      log.error("unsupported resource writing fail")

  }

  def afterEventPersisted(evt: Event): Unit = {
    updateState(evt)
    sender ! state
  }

  def updateState(evt: Event): Unit = evt match {
    case ResourceCreated(name, groups) =>
      state = Resource(name, groups)
      log.info(s"${self.path.name} switch to idle")
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
        //log.debug(s"this child size is ${child.size}")
        Resume
      }
    }
}
