package com.example

import akka.actor._
import spray.http._
import spray.httpx._
import spray.routing._
import MediaTypes._
import StatusCodes._

import UserAggregateManager._

trait UserService extends HttpService with Json4sSupport with RequestHandlerCreator {
 
  implicit val userAggregateManager: ActorRef

  var userRoute = path("user") {
    post {
      entity(as[RegisterUser]) { command =>
        implicit ctx =>
          handle(command)
      }
    }
  }
}
