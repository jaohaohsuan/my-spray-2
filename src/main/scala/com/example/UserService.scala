package com.example

import akka.actor._
import spray.http._
import spray.httpx._
import spray.routing._
import MediaTypes._
import StatusCodes._
import net.hamnaberg.json.collection._

trait UserService extends HttpService with RequestHandlerCreator with CollectionJsonSupport{

  implicit val userAggregateManager: ActorRef

  //do not change order
  import UserAggregateManager._


  var userRoute = path("user") {
    post {
      entity(as[RegisterUser]) { command =>
        implicit ctx =>
          handle(command)
          //ctx.complete(NotAcceptable)
      }
    }
  }
}
