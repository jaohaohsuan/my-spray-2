package com.example

import akka.actor._
import spray.http._
import spray.httpx._
import spray.routing._
import MediaTypes._
import StatusCodes._
import net.hamnaberg.json.collection._
import CollectionJsonProtocol._

trait UserService extends HttpService with RequestHandlerCreator with CollectionJsonSupport{

  implicit val userAggregateManager: ActorRef

  //do not change order
  import UserAggregateManager._

  var userRoute = path("user") {
    respondWithMediaType(`application/vnd.collection+json`){
      post {
        entity(as[RegisterUser]) { command => implicit ctx =>
            handle(command)
        }
      }
    }
  }
}