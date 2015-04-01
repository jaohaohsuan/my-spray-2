package com.example

import java.net.URI

import akka.actor.SupervisorStrategy._
import akka.actor.{ActorRef, _}
import com.example.UserAggregateManager.{PasswordStrengthError, BlankUsername}
import net.hamnaberg.json.collection.{Link, Item, JsonCollection, Error}
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes._
import spray.routing.{RejectionHandler, RequestContext, Rejection}


case class RegisterUserRejection(reason: String) extends Rejection

case class RegisterUserRequestActor(
                                     rtx: RequestContext,
                                     aggregateManager: ActorRef,
                                     message: AggregateManager.Command) extends RequestHandler {

  import UserAggregate._

  implicit val uri = rtx.request.uri

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
    case u@User(name, _) =>
      response {
        inspect
        respondWithHeader(RawHeader(s"Location", s"/profile/info")) {
          val href = URI.create(s"${uri.withPath(spray.http.Uri.Path("/profile/info"))}")
          val item = Item(href, u, Nil)
          complete(Created, JsonCollection(item))
        }
      }
  }

  def inspect(implicit url: spray.http.Uri): Unit = {

  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e: Throwable => {
        Resume
      }
    }
}

