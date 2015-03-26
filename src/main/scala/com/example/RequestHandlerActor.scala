package com.example

import akka.actor._
import spray.http.StatusCodes._
import spray.routing._

import scala.concurrent.duration._

trait RequestHandler extends Actor with Directives with CollectionJsonSupport with ActorLogging {

  import context._

  def rtx: RequestContext
  def aggregateManager: ActorRef
  def message: AggregateManager.Command

  setReceiveTimeout(2.seconds)
  aggregateManager ! message

  def receive = processResult orElse defaultReceive

  def processResult: Receive

  private def defaultReceive: Receive = {
    case ReceiveTimeout =>
      response {
        complete(GatewayTimeout, "Timeout")
      }
    case res =>
      response {
        complete(InternalServerError, res)
      }
  }

  def response(finalStep: Route): Unit = {
    finalStep(rtx)
    stop(self)
  }
}

trait RequestHandlerCreator {
  self: HttpService =>

  import UserAggregateManager._
  import ResourceAggregateManagerProtocol._
  import UserAggregate.{ User }

  def handle(message: AggregateManager.Command)(implicit rtx: RequestContext, aggregateManager: ActorRef, user: Option[User] = None) =
    message match {
      case _: RegisterUser => actorRefFactory.actorOf(Props(RegisterUserRequestActor(rtx, aggregateManager, message)))
      case _: ChangeUserPassword => actorRefFactory.actorOf(Props(ChangeUserPasswordRequestActor(rtx, aggregateManager, message)))
      case _: CreateResource => actorRefFactory.actorOf(Props(CreateResourceRequestActor(rtx, aggregateManager, user, message)))
    }
}
