package com.example

import akka.actor._
import spray.http.StatusCodes._
import spray.routing._
import java.net.URI
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
    respondWithMediaType(CollectionJsonProtocol.`application/vnd.collection+json`) {
        finalStep
    }(rtx)
    stop(self)
  }

}

