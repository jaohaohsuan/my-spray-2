package com.example

import java.util.UUID

import akka.actor._
import akka.persistence._
import com.example.UserAggregateManager.GetUser
import com.github.t3hnar.bcrypt._

trait State

trait Command

trait Event

case object GetState extends Command

case object Uninitialized extends State

object UserAggregate {

  case class User(name: String, pass: String) extends State
  
  case class Initialize(pass: String) extends Command

  case class ChangePassword(pass: String) extends Command

  case class UserAdded(name: String, id: String) extends Event

  case class UserInitialized(encryptedPass: String) extends Event

  case class UserPasswordChanged(encryptedPass: String) extends Event

  def props(id: String): Props = Props(new UserAggregate(id))

  trait Error

  case object UserExist extends Error

}

class UserAggregate(uuid: String) extends PersistentActor with ActorLogging {

  import com.example.UserAggregate._

  override def persistenceId = uuid

  var state: State = Uninitialized

  //only concern about how to create event, do not write anything after persisted event logic

  val initial: Receive = {
    case Initialize(pass) if pass.length > 5 =>
      persist(UserInitialized(pass.bcrypt))(afterEventPersisted)
  }

  val created: Receive = {
    case ChangePassword(newPass) if newPass.length > 5 =>
      persist(UserPasswordChanged(newPass.bcrypt))(afterEventPersisted)
    case GetState =>
      sender ! state
    case e =>
      log.error(s"unexpected $e")
  }

  def afterEventPersisted(evt: Event): Unit = {
    updateState(evt)
    sender ! state
  }

  def updateState(evt: Event): Unit = evt match {
    case UserInitialized(pass) =>
      state = User(self.path.name, pass)
      context.become(created)
    case UserPasswordChanged(newPass) =>
      state match {
        case s: User =>
          state = s.copy(pass = newPass)
      }
  }

  val receiveCommand: Receive = initial

  val receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
    case SnapshotOffer(_, snapshot: State) =>
      state = snapshot
      state match {
        case _: User => context become created
      }
  }

}

