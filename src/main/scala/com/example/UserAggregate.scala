package com.example

import akka.actor._
import akka.persistence._
import com.github.t3hnar.bcrypt._

trait State
trait Command
trait Event
case object GetState extends Command


case object Uninitialized extends State

object UserAggregate {

  case class User(id: String, pass: String) extends State

  case class Initialize(pass: String) extends Command
  case class ChangePassword(pass: String) extends Command

  case class UserInitialized(encryptedPass: String) extends Event
  case class UserPasswordChanged(encryptedPass: String) extends Event



  def props(id: String): Props = Props(new UserAggregate(id))
}

class UserAggregate(id: String) extends PersistentActor {

  import com.example.UserAggregate._

  override def persistenceId = id

  var state: State = Uninitialized

  val initial: Receive = {
    case Initialize(pass) =>
      //only concern about how to create event, do not write anything after persisted event logic
      val encryptedPass = pass.bcrypt
      persist(UserInitialized(encryptedPass))(afterEventPersisted)
  }

  val created: Receive = {
    case ChangePassword(newPass) =>
      val newPassEncrypted = newPass.bcrypt
      persist(UserPasswordChanged(newPassEncrypted))(afterEventPersisted)
    case GetState =>
      sender ! state
    case _: Initialize =>
      sender ! "User has been initialized."
  }

  def afterEventPersisted(evt: Event): Unit = evt match {
    case UserInitialized(pass) =>
      context.become(created)
      state = User(id, pass)
      sender ! state
    case UserPasswordChanged(newPass) =>
      state match {
        case s: User =>
          state = s.copy(pass = newPass)
          sender ! state
      }
  }

  val receiveCommand: Receive = initial

  val receiveRecover: Receive = {
    case _ =>
  }

}

