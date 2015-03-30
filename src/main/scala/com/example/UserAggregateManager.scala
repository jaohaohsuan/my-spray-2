package com.example

import java.util.UUID

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import akka.persistence._

object UserAggregateManager {

  import UserAggregate.{ Error }

  case class RegisterUser(name: String, pass: String) extends AggregateManager.Command
  case class ChangeUserPassword(username: String, pass: String) extends AggregateManager.Command
  case class GetUser(name: String)

  def props: Props = Props(new UserAggregateManager)

  case object BlankUsername extends Error
  case object PasswordStrengthError extends Error

}

class UserAggregateManager extends PersistentActor with ActorLogging {

  import com.example.UserAggregate._
  import com.example.UserAggregateManager._

  override def persistenceId = "userAggregateManager"

  var users = Map[String, String]()
  var keys = Set[String]()

  def genUniqueId(id: String = UUID.randomUUID().toString): String = if (keys.contains(id)) genUniqueId() else id

  val processRegister: Receive = {

    case RegisterUser("", _) =>
      sender ! BlankUsername

    case RegisterUser(_, password) if password.length < 5 =>
      sender ! PasswordStrengthError

    case RegisterUser(name, _) if users.contains(name) =>
      sender ! UserExist

    case RegisterUser(name, pass) =>
      persist(UserAdded(name, genUniqueId())) { evt =>
        updateState(evt)
        context.actorOf(Props(classOf[UserAggregate], evt.id), evt.name) forward Initialize(pass)
      }

  }

  val processChanges: Receive = {

    case ChangeUserPassword(_, pass) if pass.length < 5 =>
      sender ! PasswordStrengthError

    case ChangeUserPassword(username, pass) if users.contains(username) =>
      context.child(username) match {
        case Some(child) =>
          child forward ChangePassword(pass)
        case None =>
          log.error(s"why do not you ask GetUser")
      }
  }

  val processReading: Receive = {
    case msg @ GetUser(name) =>
      users.get(name) match {
        case Some(id) =>
          context.child(name).getOrElse(context.actorOf(Props(classOf[UserAggregate], id), name)) forward GetState
        case None =>
          log.info(s"$name is not exist")
          sender ! Uninitialized
      }

    case Terminated(child) =>

  }

  def receiveCommand: Receive = processRegister.orElse(processReading).orElse(processChanges).orElse({
    case msg =>
      log.error(s"unexpected $msg")
  })

  def updateState(evt: Event): Unit = evt match {
    case UserAdded(name, id) =>
      users = users + (name -> id)
      keys = keys + id
  }

  val receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
    case SnapshotOffer(_, snapshot) =>
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        Resume
      }
    }
}

