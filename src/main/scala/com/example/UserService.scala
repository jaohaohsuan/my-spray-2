package com.example

import java.net.URI

import akka.actor._
import com.example.CollectionJsonProtocol._
import net.hamnaberg.json.collection._
import spray.http.StatusCodes._
import spray.routing._
import spray.routing.authentication.BasicAuth

object UserService {

  case class ChangePasswordRequest(pass: String)

}

trait UserService extends HttpService with RequestHandlerCreator with CollectionJsonSupport with UserAuthenticator {

  implicit val userAggregateManager: ActorRef

  //do not change order

  import com.example.UserAggregateManager._
  import com.example.implicitTemplateConversions._

  val globalUri = URI.create("http://example.com/user")

  var userRoute = pathPrefix("user") {
    respondWithMediaType(`application/vnd.collection+json`) {
      pathEndOrSingleSlash {
        get {
          complete(OK,
            JsonCollection(globalUri, Nil, Nil, Nil, RegisterUser("username", "password"))
          )
        } ~
          post {
            entity(as[RegisterUser]) { command =>
              implicit ctx =>
                handle(command)
            }
          }
      } ~ path("password") {
        get {
          complete(OK,
            JsonCollection(globalUri, Nil, Nil, Nil, ChangeUserPassword("", ""))
          )
        }
        put {
          authenticate(BasicAuth(userAuthenticator _, realm = "personal")) { user =>
            entity(as[UserService.ChangePasswordRequest]) { e =>
              implicit ctx =>
                handle(ChangeUserPassword(user.id, e.pass))
            }
          }
        }
      }
    }
  } ~ path("profile" / Segment) { resource =>
    authenticate(BasicAuth(userAuthenticator _, realm = "personal")) { user: UserAggregate.User =>
      get {
        val link = Link(URI.create("/user/password"), "edit", None, None, None)
        val item = Item(URI.create("/profile"), user, List(link))
        complete(OK, resource, JsonCollection(item))
      }
    }
  }

}

