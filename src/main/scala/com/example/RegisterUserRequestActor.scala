package com.example

import java.net.URI

import akka.actor.SupervisorStrategy._
import akka.actor.{ ActorRef, _ }
import net.hamnaberg.json.collection.{ Error, JsonCollection }
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes._
import spray.routing.RequestContext

case class RegisterUserRequestActor(
    rtx: RequestContext,
    aggregateManager: ActorRef,
    message: AggregateManager.Command) extends RequestHandler {

  val uri = URI.create("http://com.example/user")

  import com.example.UserAggregate._

  def processResult = {
    case error: String =>
      response {
        complete(NotAcceptable, JsonCollection(uri, Error(title = "RegisterUser", code = None, message = Some(error))))
      }
    case User(name, _) =>
      response {
        respondWithHeader(RawHeader(s"Location", s"/user/$name")) {
          //val item = Item(URI.create("/user/henry"), u, List[net.hamnaberg.json.collection.Link]())
          //complete(Accepted,JsonCollection(URI.create("http://com.example/user"),List[Link](),item))
          complete(Accepted)
        }
      }
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {

        Resume
      }
    }
}
