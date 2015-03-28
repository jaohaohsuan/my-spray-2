package com.example

import akka.actor.SupervisorStrategy.Resume
import akka.actor._

object UserAggregateManager {

  import UserAggregate.{ Error }

  case class RegisterUser(name: String, pass: String) extends AggregateManager.Command
  case class ChangeUserPassword(id: String, pass: String) extends AggregateManager.Command
  case class GetUser(name: String)

  def props: Props = Props(new UserAggregateManager)

  case object BlankUsername extends Error
  case object PasswordStrengthError extends Error

}

class UserAggregateManager extends Actor with ActorLogging {

  import com.example.UserAggregate._
  import com.example.UserAggregateManager._

  def receive = {
    case RegisterUser("", _) =>
      sender ! BlankUsername

    case RegisterUser(_, password) if password.length < 5 =>
      sender ! PasswordStrengthError

    case RegisterUser(name, pass) =>
      implicit val id = name
      context child id getOrElse create(context.watch) forward Initialize(pass)

    case GetUser(name) =>
      implicit val id = name
      context child id getOrElse create(context.watch) forward GetState

    case ChangeUserPassword(_, pass) if pass.length < 5 =>
      sender ! PasswordStrengthError

    case ChangeUserPassword(id, pass) =>

      context.child(id) match {
        case Some(u) => u forward ChangePassword(pass)
        case None => sender ! Uninitialized
      }

    case Terminated(child) =>
      log.info(s"${child.path.name} is not exist")
  }

  def create(f: ActorRef => ActorRef)(implicit userAggregateId: String): ActorRef = {
    f(context.actorOf(UserAggregate.props(userAggregateId), userAggregateId))
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        Resume
      }
    }
}

