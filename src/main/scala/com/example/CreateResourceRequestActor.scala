package com.example

import java.net.URI

import akka.actor.SupervisorStrategy._
import akka.actor.{ ActorRef, _ }
import net.hamnaberg.json.collection.{ Error, JsonCollection }
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes._
import spray.routing.RequestContext
import UserAggregate.{ User }

case class CreateResourceRequestActor(
    rtx: RequestContext,
    aggregateManager: ActorRef,
    user: Option[User],
    message: AggregateManager.Command) extends RequestHandler {

  val uri = URI.create("http://com.example/resources")

  import com.example.ResourceProtocol._
  import PermissionProtocol._

  def processResult = {

    case msg: Handshaking =>
      user match {
        case Some(User(name, _)) =>
          sender ! msg.copy(user = Some(OwnerConfirmed(name, Set(name))))
        case None =>
          log.error("unauthenticate user")
      }

    case Resource(name, _) =>
      response {
        respondWithHeader(RawHeader(s"Location", s"/resource/$name")) {
          complete(Accepted)
        }
      }

    case error: String =>
      response {
        complete(NotAcceptable, JsonCollection(uri, Error(title = "CreateResource", code = None, message = Some(error))))
      }
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e: Throwable => {

        Resume
      }
    }
}
