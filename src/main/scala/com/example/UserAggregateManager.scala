package com.example

import akka.actor._

object UserAggregateManager {

  import AggregateManager._

  case class RegisterUser(name: String, pass: String) extends Command

  def props: Props = Props(new UserAggregateManager)

}


//UserAggregate's parent(supervisor)
class UserAggregateManager extends Actor {

  import UserAggregateManager._
  import UserAggregate._

  def receive = {
    case RegisterUser("",_) => sender ! "You can not register without name."
    case RegisterUser(name, pass) =>
      implicit val id = s"user-$name"
      context child id getOrElse create(context.watch) forward Initialize(pass)
  }  

  def create(f: ActorRef => ActorRef)(implicit userAggregateId: String): ActorRef = {
   f(context.actorOf(UserAggregate.props(userAggregateId),userAggregateId))
  }
}

