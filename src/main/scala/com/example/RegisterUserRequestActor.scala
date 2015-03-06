package com.example

import java.net.URI

import akka.actor.ActorRef
import net.hamnaberg.json.collection.{Error, JsonCollection}
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes._
import spray.routing.RequestContext

case class RegisterUserRequestActor(
                                     rtx: RequestContext,
                                     aggregateManager: ActorRef,
                                     message: AggregateManager.Command) extends RequestHandler {

  import com.example.UserAggregate._

  def processResult = {
    case error: String =>
      response {
        complete(NotAcceptable, JsonCollection(URI.create("http://com.example/user"), Error(title = "RegisterUser", code = None, message = Some(error))))
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
}
