package com.example

import akka.actor._
import akka.persistence._

trait State
trait Command
trait Event

case object Uninitialized extends State

object UserAggregate {
  
  case class User(name: String, pass: String) extends State
  case class Initialize(pass: String) extends Command
  case class UserInitialized(encryptedPass: String) extends Event

  def props(id: String): Props = Props(new UserAggregate(id))
}

class UserAggregate(id: String) extends PersistentActor {

  import UserAggregate._

  override def persistenceId = id

  var state: State = Uninitialized

  val initial: Receive = {
    case Initialize(pass) =>
      //only concern about how to create event, do not write anything after persisted event logic
      val encryptedPass = pass
      persist(UserInitialized(encryptedPass))(afterEventPersisted)
  }

  val created: Receive = {
    case _ => 
  }
  
  def afterEventPersisted(evt: Event): Unit = evt match {
    case UserInitialized(pass) =>
      context.become(created)
      state = User(id, pass)
      sender ! state
  }

  val receiveCommand: Receive = initial

  val receiveRecover: Receive = {
    case _ => 
  }

}


