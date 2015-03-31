package com.example

import java.net.URI

import akka.actor.SupervisorStrategy._
import akka.actor.{ ActorRef, _ }
import com.example.UserAggregateManager.{ PasswordStrengthError, BlankUsername }
import net.hamnaberg.json.collection.{ JsonCollection, Error }
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes._
import spray.routing.{ RejectionHandler, RequestContext, Rejection }

object JsonCollectionExtensions {

}

case class RegisterUserRejection(reason: String) extends Rejection

case class RegisterUserRequestActor(
    rtx: RequestContext,
    aggregateManager: ActorRef,
    message: AggregateManager.Command) extends RequestHandler {

  import UserAggregate._

  def processResult = {

    case BlankUsername =>
      response {
        reject(RegisterUserRejection("blank name is not allowed"))
      }
    case PasswordStrengthError =>
      response {
        reject(RegisterUserRejection("password length is too short"))
      }
    case Uninitialized =>
      response {
        reject(RegisterUserRejection("user is not exist"))
      }
    case UserExist =>
      response {
        reject(RegisterUserRejection("use another name"))
      }
    case User(name, _) =>
      response {
        respondWithHeader(RawHeader(s"Location", s"/profile/info")) {
          complete(Created)
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

