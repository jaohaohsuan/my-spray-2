package com.example

import java.net.URI

import akka.actor.SupervisorStrategy._
import akka.actor.{ ActorRef, _ }
import com.example.UserAggregateManager.{PasswordStrengthError, BlankUsername}
import net.hamnaberg.json.collection.{ Error, JsonCollection }
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes._
import spray.routing.RequestContext

object JsonCollectionExtensions {

  implicit def autoConvert(message: String)(implicit uri: URI) = new {
    def asJsonCollection = {
      JsonCollection(uri, Error(title = "user/error", code = None, message =
        Some(message)))
    }
  }
}

case class RegisterUserRequestActor(
    rtx: RequestContext,
    aggregateManager: ActorRef,
    message: AggregateManager.Command) extends RequestHandler {

  implicit val uri = URI.create("http://com.example/user")

  import UserAggregate._
  import JsonCollectionExtensions._

  def processResult = {

    case BlankUsername =>
      response {
        complete(NotAcceptable, "blank name is not allowed".asJsonCollection)
      }
    case PasswordStrengthError =>
      response {
        complete(NotAcceptable, "password length is too short".asJsonCollection)
      }
    case Uninitialized =>
      response {
        complete(NotAcceptable, "user is not exist".asJsonCollection)
      }
    case UserExist =>
      response {
        complete(NotAcceptable, "use another name".asJsonCollection)
      }
    case User(name, _) =>
      response {
        respondWithHeader(RawHeader(s"Location", s"/profile/info")) {
          complete(Accepted)
        }
      }
  }



  override val supervisorStrategy =
    OneForOneStrategy() {
      case e: Throwable => {
        Resume
      }
    }
}



