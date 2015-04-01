package com.example

import akka.actor.{ActorRef, Actor, ActorLogging, ReceiveTimeout}
import net.hamnaberg.json.collection.JsonCollection
import spray.http.StatusCodes._
import spray.routing.{Directives, RequestContext, Route}
import scala.language.postfixOps
import scala.concurrent.duration._

/**
 * Created by henry on 4/1/15.
 */
class AccessResourceRequestActor(rtx: RequestContext, keeper: ActorRef, command: Any) extends Actor with Directives with CollectionJsonSupport with ActorLogging
{

  import context._
  import resources.Keeper._

  setReceiveTimeout(2 seconds)

  keeper ! command

  def receive = processResult orElse defaultReceive

  def processResult: Receive = {
    case ResourceContent(content) =>
//      response {
//        complete(OK, asCollectionJson(content))
//      }

  }

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

  def asCollectionJson(data: AnyRef)(implicit uri: spray.http.Uri) = {

    JsonCollection(java.net.URI.create(s"$uri"),Nil, Nil, Nil, None)
  }


  def response(finalStep: Route): Unit = {
    requestUri { uri =>
      ctx =>
        finalStep(ctx)
        stop(self)
    }(rtx)
  }
}
