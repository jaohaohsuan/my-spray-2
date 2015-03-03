package com.example

import akka.actor._
import spray.routing._
import spray.http.StatusCodes._
import net.hamnaberg.json.collection._
import spray.httpx._
import scala.concurrent.duration._

case class RegisterUserRequestActor(
  rtx              : RequestContext,
  aggregateManager : ActorRef,
  message          : AggregateManager.Command) extends RequestHandler {

  def processResult = {
    case error: String => 
      rtx.complete(NotAcceptable, Error(title = "RegisterUser", code = None, message = Some(error)))
  }
}

trait RequestHandler extends Actor with Json4sSupport {
  
  import context._

  val json4sFormats = org.json4s.DefaultFormats

  def rtx: RequestContext
  def aggregateManager: ActorRef
  def message: AggregateManager.Command

  setReceiveTimeout(2.seconds)
  aggregateManager ! message

  def receive = processResult orElse defaultReceive

  def processResult: Receive

  private def defaultReceive: Receive = {
    case ReceiveTimeout =>
      rtx.complete(GatewayTimeout)
    case res =>
      rtx.complete(InternalServerError)
  }
}

trait RequestHandlerCreator {
  self: HttpService =>

  import UserAggregateManager._

  def handle(message: AggregateManager.Command)(implicit rtx: RequestContext, aggregateManager: ActorRef) =
    
    message match {
      case _:RegisterUser => actorRefFactory.actorOf(Props(RegisterUserRequestActor(rtx, aggregateManager, message)))
      
    }
}
