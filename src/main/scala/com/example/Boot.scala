package com.example
import akka.actor._
import akka.io.IO
import akka.util.Timeout
import spray.can.Http
import scala.concurrent.duration._
import spray.routing.HttpServiceActor


class ServiceActor extends HttpServiceActor with Demo1Service with SecurityService with ExceptionHandlingService
{
  def executionContext = context.dispatcher

  def receive = runRoute(demo1Route ~ securityRoute ~ exceptionHandlingRoute)
}

object Boot extends App {

  implicit val system = ActorSystem("demo1")

  val service = system.actorOf(Props[ServiceActor], name = "service")

  implicit val timeout = Timeout(5.seconds)

  IO(Http) ! Http.Bind(service, interface = "localhost", port = 1978)
}
