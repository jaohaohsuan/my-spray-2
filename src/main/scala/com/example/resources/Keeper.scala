package com.example.resources

import com.example._

/**
 * Created by henry on 3/31/15.
 */

import akka.actor._
import akka.persistence._

object Keeper {

  case class ResourceContent(content: AnyRef)
  case object NotFound
  case object AccessGained
  case object AccessDenied
  case object ResourceSaved
  case class Registered(user: String, path: String, structure: String) extends Event

  case class Get(user: String, path: String, structure: String)
  case class UpdateResource(user: String, path: String, structure: String, content: AnyRef)

}

class Keeper extends PersistentActor with ActorLogging {

  import Officer._
  import Repository._
  import Keeper._

  val repository: ActorRef = context.actorOf(Props[Repository])

  override def persistenceId = "resource-keeper"

  var state = Map[String, Set[(String, String)]]()

  val receiveCommand: Receive = {

    case Register(user, path, structure) =>
      persist(Registered(user, path, structure)) { evt =>
        updateState(evt)
        sender ! AccessGained
      }

    case msg @ Get(user, path, structure) =>
      find(user, path, structure) match {
        case Some(_) =>
          repository forward msg
        case None =>
          sender ! AccessDenied
      }

    case UpdateResource(user, path, structure, content) =>
      find(user, path, structure) match {
        case Some(_) =>
          repository forward UpdateRepository(path, structure, content)
        case None =>
          sender ! AccessDenied
      }

    case e: Any =>
      log.error(s"unexpected message '$e'")
  }

  def find(user: String, path: String, structure: String): Option[(String, String)] = {
    state.get(user).flatMap(_.find { case (p, s) => path.contains(p) && structure.contains(s) })
  }

  def updateState(evt: Event): Unit = evt match {
    case Registered(user, path, structure) =>
      state = state + (user -> (state.getOrElse(user, { Set() }).+((path, structure))))
  }

  val receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
    case SnapshotOffer(_, snapshot: State) =>

  }

}