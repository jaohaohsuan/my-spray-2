package com.example

import java.net.URI

import akka.actor.SupervisorStrategy._
import akka.actor.{ ActorRef, _ }
import com.example.UserAggregateManager.{PasswordStrengthError, BlankUsername}
import net.hamnaberg.json.collection.{Error, JsonCollection}
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes._
import spray.routing.RequestContext

object JsonCollectionExtensions {


}

case class RegisterUserRequestActor(
    rtx: RequestContext,
    aggregateManager: ActorRef,
    message: AggregateManager.Command) extends RequestHandler {

  implicit val uri = URI.create("http://com.example/user")

  import UserAggregate._

  def processResult = {

    case BlankUsername =>
      response {
        complete(NotAcceptable, asJsonCollection("blank name is not allowed"))
      }
    case PasswordStrengthError =>
      response {
        complete(NotAcceptable, asJsonCollection("password length is too short"))
      }
    case Uninitialized =>
      response {
        complete(NotAcceptable, asJsonCollection("user is not exist"))
      }
    case UserExist =>
      response {
        complete(NotAcceptable, asJsonCollection("use another name"))
      }
    case User(name, _) =>
      response {
        respondWithHeader(RawHeader(s"Location", s"/profile/info")) {
          complete(Accepted)
        }
      }
  }

  def asJsonCollection(message: String) = {
    JsonCollection(uri, Error(title = "user/error", code = None, message =
      Some(message)))
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e: Throwable => {
        Resume
      }
    }
}



