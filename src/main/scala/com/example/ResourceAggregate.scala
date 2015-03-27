package com.example

import akka.actor._
import akka.persistence._
import java.util.{UUID}
import akka.actor.SupervisorStrategy.{Resume, Stop}
import akka.pattern.{ask, pipe}
import com.example.ResourceProtocol.CreatingResource
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader

object ResourceAggregate {

  case class Created(owner: String, groups: Set[String]) extends Event

  case class NewChild(name: String) extends Event

}

class ResourceAggregate(uuid: String) extends PersistentActor with ActorLogging {

  override def persistenceId = uuid

  var state: State = Uninitialized

  import ResourceProtocol._
  import ResourceAggregate._

  def receiveCommand = {

    case c@CreatingResource(x :: xs, Some(owner), Some(groups)) =>

      persist(Created(owner, groups)) { evt =>
        updateState(evt)
        xs match {
          case Nil =>
            log.info(s"'$x' is the last one")
            sender ! state
          case _ =>
            log.info(s"'$x' is created")
        }
      }

    case _ =>
      sender ! Uninitialized

  }

  val established: Receive = {

    case GetState =>
      sender ! state

    case c@CreatingResource(x :: xs, Some(owner), Some(groups)) =>

      state match {

        case ResourceState(_, _, children) if children.contains(x) =>
          sender ! new Exception(s"$x is already exist")

        case _: ResourceState =>

          val child = context.child(x).getOrElse( {
            val resource = context.actorOf(Props(classOf[ResourceAggregate], UUID.randomUUID().toString), x)
            resource forward c
            persist(NewChild(x)){ evt => updateState(evt)}
            resource
          })

          xs match {
            case Nil =>
              log.info("no more child to create")
            case _ =>
              child forward c.copy(xs)
          }
      }
  }

  def updateState(evt: Event): Unit = evt match {
    case Created(owner, groups) =>
      state = ResourceState(owner, groups)
      context.become(established)
    case NewChild(name) =>
      state match {
        case r: ResourceState =>
          state = r.copy(children = r.children + name)
      }

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
    }
}
