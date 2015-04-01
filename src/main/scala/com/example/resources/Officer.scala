package com.example.resources

import com.example._
/**
 * Created by henry on 3/31/15.
 */

import akka.actor._
import akka.persistence._

object Officer {

  case class Register(user: String, path: String, structure: String)
  case class Cancel(id: String)

}

class Officer extends PersistentActor with ActorLogging {

  import Officer._

  override def persistenceId = "officer"

  val keeper: ActorRef = context.actorOf(Props[Keeper])

  var state: State = Uninitialized

  def afterEventPersisted(evt: Event): Unit = {
    updateState(evt)
    sender ! state
  }

  def updateState(evt: Event): Unit = evt match {
    case _ => //state = ...
  }

  val receiveCommand: Receive = {
    case msg: Register =>
      keeper forward msg

    case e: Any =>
      println(s"$e")
  }

  val receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
    case SnapshotOffer(_, snapshot: State) =>
      state = snapshot
    //context might become different
  }

}