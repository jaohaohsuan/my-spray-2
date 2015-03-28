package com.example

import java.net.URI

import akka.actor.SupervisorStrategy._
import akka.actor._
import com.example.UserAggregateManager.GetUser
import net.hamnaberg.json.collection.{ Error, JsonCollection }
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes._
import spray.routing.RequestContext
import UserAggregate.{ User }

case class CreateResourceRequestActor(
    rtx: RequestContext,
    aggregateManager: ActorRef,
    user: User,
    message: AggregateManager.Command) extends RequestHandler {

  val uri = URI.create("http://com.example/resources")

  import ResourceProtocol._

  def processResult: Receive = {
    case cmd: CreatingResource =>
      sender ! cmd.copy(owner = Some(user.name), groups = Some(Set("admin")))
    case r: ResourceState =>
      response {
        complete(OK, r)
      }
    case _ =>
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e: Throwable => {

        Resume
      }
    }
}
