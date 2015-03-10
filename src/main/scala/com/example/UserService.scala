package com.example

import akka.actor._
import com.example.CollectionJsonProtocol._
import spray.routing._
import spray.routing.authentication.BasicAuth

object UserService {
  case class ChangePasswordRequest(pass: String)
}

trait UserService extends HttpService with RequestHandlerCreator with CollectionJsonSupport with UserAuthenticator {

  implicit val userAggregateManager: ActorRef

  //do not change order
  import com.example.UserAggregateManager._

  var userRoute = pathPrefix("user") {
    respondWithMediaType(`application/vnd.collection+json`) {
      post {
        pathEndOrSingleSlash {
          entity(as[RegisterUser]) { command =>
            implicit ctx =>
              handle(command)
          }
        }
      } ~
        put {
          path("password") {
            authenticate(BasicAuth(userAuthenticator _, realm = "profile")) { user =>
              entity(as[UserService.ChangePasswordRequest]) { e =>
                implicit ctx =>
                  handle(ChangeUserPassword(user.id, e.pass))
              }
            }
          }
        }
    }
  }
}
