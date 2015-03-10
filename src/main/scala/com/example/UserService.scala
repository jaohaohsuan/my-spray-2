package com.example

import java.net.URI

import akka.actor._
import com.example.CollectionJsonProtocol._
import net.hamnaberg.json.collection._
import net.hamnaberg.json.collection.data._
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

  val globalUri = URI.create("http://example.com/user")

  var userRoute = pathPrefix("user") {
    respondWithMediaType(`application/vnd.collection+json`) {
      pathEndOrSingleSlash {
        get {
          complete(OK,
            JsonCollection(globalUri, List[Link](), List[Item](), List[Query](), RegisterUser("username", "password"))
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
                JsonCollection(globalUri, List[Link](), List[Item](), List[Query](), ChangeUserPassword("", ""))
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
        val link = Link(URI.create("/user/password"),"edit", None,None,None)
        val item = Item(URI.create("/profile"), user, List(link))
        complete(OK, resource, JsonCollection(item))
      }
    }
  }

  implicit def dataApply[T <: AnyRef : Manifest](implicit formats: org.json4s.Formats): DataApply[T] = {
    new JavaReflectionData[T]()(formats, manifest[T])
  }

  implicit def asTemplate[T <: AnyRef : Manifest](value: T)(implicit formats: org.json4s.Formats): Option[Template] =
    Some(Template(value)(dataApply(manifest,formats)))

}
