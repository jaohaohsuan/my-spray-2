package com.example

import akka.actor.ActorRef
import spray.http.StatusCodes._
import spray.routing.RequestContext

case class ChangeUserPasswordRequestActor(
    rtx: RequestContext,
    aggregateManager: ActorRef,
    message: AggregateManager.Command) extends RequestHandler {

  import com.example.UserAggregate._

  def processResult = {

    case User(name, _) =>
      response {
        complete(OK)
      }
  }

}
