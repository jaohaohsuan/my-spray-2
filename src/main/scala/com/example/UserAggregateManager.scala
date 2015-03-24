package com.example

import akka.actor.SupervisorStrategy.Resume
import akka.actor._

object UserAggregateManager {

  case class RegisterUser(name: String, pass: String) extends AggregateManager.Command
  case class ChangeUserPassword(id: String, pass: String) extends AggregateManager.Command
  case class GetUser(name: String)

  def props: Props = Props(new UserAggregateManager)

}

//UserAggregate's parent(supervisor)
class UserAggregateManager extends Actor with ActorLogging {

  import com.example.UserAggregate._
  import com.example.UserAggregateManager._

  def receive = {
    case RegisterUser("", _) => sender ! "You can not register without name."
    case RegisterUser(_, password) if password.length < 5 => sender ! "password length is too short"
    case RegisterUser(name, pass) =>
      implicit val id = s"user-$name"
      context child id getOrElse create(context.watch) forward Initialize(pass)
    case GetUser(name) =>
      val id = s"user-$name"
      context child id match {
        case Some(child) =>
          child forward GetState
        case None =>
          sender ! "User is not exist."
      }
    case ChangeUserPassword(_, pass) if pass.length < 5 =>
      sender ! "password length is too short"
    case ChangeUserPassword(id, pass) =>
      context child id getOrElse create(context.watch)(id) forward ChangePassword(pass)
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

