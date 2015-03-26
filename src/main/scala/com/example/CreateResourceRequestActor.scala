package com.example

import java.net.URI

import akka.actor.SupervisorStrategy._
import akka.actor.{ ActorRef, _ }
import com.example.UserAggregateManager.GetUser
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

  import ResourceProtocol._
  import PermissionProtocol._

  val currentUser = user.map(value => {
    val User(name, _) = value
    OwnerConfirmed(name, Set(name, "root"))
  })

  def processResult: Receive = {

    case GetOwner =>
      currentUser match {
        case Some(u) =>
          sender ! u
        case None => log.error("fail to get owner")
      }
    case msg: Handshaking =>
      sender ! msg.copy(user = currentUser)

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

    case e: AnyRef =>
      response {
        complete(InternalServerError, s"unexpected message $e")
      }
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e: Throwable => {

        Resume
      }
    }
}
