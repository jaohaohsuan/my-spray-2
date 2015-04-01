package com.example.resources

import javax.swing.text.AbstractDocument.Content

import com.example._
import com.example.resources.Repository.ResourceChanged

/**
 * Created by henry on 3/31/15.
 */

import akka.actor._
import akka.persistence._

object Repository {

  case class ResourceChanged(path: String, structure: String, content: AnyRef) extends Event

  case class UpdateRepository(path: String, structure: String, content: AnyRef)

}

class Repository extends PersistentActor with ActorLogging {

  import Keeper._
  import Repository._

  type MapsStore = Map[String, Map[String, AnyRef]]

  override def persistenceId = "resource-repo"

  var state: MapsStore = Map[String, Map[String, AnyRef]]()

  val receiveCommand: Receive = {

    case UpdateRepository(path, structure, content) =>
      persist(ResourceChanged(path, structure, content)) { evt =>
        updateState(evt)
        sender ! ResourceSaved
      }

    case Get(_, path, structure) =>
      state.get(structure).flatMap(_.get(path)) match {
        case Some(content) =>
          sender ! ResourceContent(content)
        case None =>
          sender ! NotFound
      }

    case e: Any =>
      log.error(s"unexpected message '$e'")
  }

  def updateState(evt: Event): Unit = evt match {
    case ResourceChanged(path, structure, content) =>
      state = state + (structure -> { state.getOrElse(structure, Map()) + (path -> content) })

  }

  val receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
    //case SnapshotOffer(_, snapshot: MapsStore) =>
    //state = snapshot
  }

}