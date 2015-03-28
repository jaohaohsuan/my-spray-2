package com.example

import akka.actor._
import akka.persistence._
import java.util.{UUID}
import akka.actor.SupervisorStrategy.{Resume, Stop}
import akka.pattern.{ask, pipe}
import com.example.ResourceProtocol.CreatingResource
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader

object ResourceAggregate {

  case class Created(content: AnyRef, owner: String, groups: Set[String]) extends Event

  case class NewChild(name: String, persistenceId: String) extends Event

}

class ResourceAggregate(uuid: String) extends PersistentActor with ActorLogging {

  override def persistenceId = uuid

  var state: State = Uninitialized

  //var permissions:

  import ResourceProtocol._
  import ResourceAggregate._

  def receiveCommand = {

    case msg@CreatingResource(x :: xs, content, Some(owner), Some(groups)) =>

      persist(Created(content ,owner, groups)) { evt =>
        updateState(evt)
        log.info(s"'$x' is created")
        xs match {
          case Nil =>
            log.info(s"'$x' is the last one")
            sender ! state
          case _ =>
            log.info(s"let's go to create further child")
            self forward msg.copy(path = xs)
        }
      }

    case _ =>
      sender ! Uninitialized

  }

  val established: Receive = {

    case GetState =>
      sender ! state

    case msg@CreatingResource(x :: xs, _, Some(owner), Some(userGroups)) =>

      state match {

        case ResourceState(_, _, groups, _) if (groups & userGroups).size == 0 =>
          sender ! "you do not have permission to access"
        case ResourceState(_, _, groups, children) =>

          val existChild: Option[ActorRef]= context.child(x) match {
            case Some(actorRef) =>
              log.info(s"$x is already exist at context child")
              Some(actorRef)
            case None =>
              children.get(x) match {
                case Some(id) =>
                  log.info(s"$x is already created before. Let's restore the ActorRef '$id'")
                  Some(context.actorOf(Props(classOf[ResourceAggregate],id), x))
                case None => None // means that you have to create new resource
              }
          }

          existChild match {
            case Some(actorRef) =>
               actorRef forward (xs match {
                  case Nil => GetState
                  case _ =>
                    log.info(s"let's go to create further child ${xs}")
                    msg.copy(path = xs)
                })
            case None =>
              log.info(s"'${self.path.name}' is creating new child")
              val newChildId = UUID.randomUUID().toString
              context.actorOf(Props(classOf[ResourceAggregate], newChildId), x) forward msg
              persist(NewChild(x, newChildId)){ evt => updateState(evt)}
          }
      }
  }

  def updateState(evt: Event): Unit = evt match {
    case Created(content ,owner, groups) =>
      state = ResourceState(content,owner, groups)
      context.become(established)
    case NewChild(name, id) =>
      state match {
        case s@ResourceState(_,_,_,children) =>
          state = s.copy(children = children + (name -> id))
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
