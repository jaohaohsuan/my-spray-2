package com.example

/**
 * Created by henry on 3/27/15.
 */

import akka.actor._
import spray.http.StatusCodes._
import spray.routing._

import scala.concurrent.duration._


trait RequestHandlerCreator {
  self: HttpService =>

  import UserAggregate.User
  import UserAggregateManager._
  import ResourceAggregateManager._

  def handle(message: AggregateManager.Command)(implicit rtx: RequestContext, aggregateManager: ActorRef, user: User) =
    message match {
      case _: RegisterUser => actorRefFactory.actorOf(Props(RegisterUserRequestActor(rtx, aggregateManager, message)))
      case _: ChangeUserPassword => actorRefFactory.actorOf(Props(ChangeUserPasswordRequestActor(rtx, aggregateManager, message)))
      case _: CreateResource => actorRefFactory.actorOf(Props(CreateResourceRequestActor(rtx, aggregateManager, user, message)))
    }
}
