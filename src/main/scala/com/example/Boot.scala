package com.example

import akka.actor._
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.sksamuel.elastic4s.ElasticClient
import com.typesafe.config.ConfigFactory
import spray.can.Http
import spray.routing.HttpServiceActor

import scala.concurrent.duration._
import scala.language.postfixOps

class ServiceActor extends HttpServiceActor with AggregateManagerCreator
    with Demo1Service
    with SecurityService
    with ExceptionHandlingService
    with UserService
    with ElasticDemo1
    with ResourceService {

  override def executionContext = context.dispatcher

  val client = ElasticClient.remote("localhost", 9300)

  val userAggregateManager = createUserAggregateManager

  val resourceAggregateManager = createResourceAggregateManager

  def receive = runRoute(demo1Route ~ securityRoute ~ exceptionHandlingRoute ~ userRoute ~ esRoute ~ resourceRoute)
}

object Boot extends App with SslConfiguration {

  val config = ConfigFactory.load()

  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  implicit val system = ActorSystem("demo1")

  val api = system.actorOf(Props[ServiceActor], name = "HttpInterface")

  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(10 seconds)

  //IO(Http) ! Http.Bind(api, interface = "localhost", port = 1978)
  IO(Http).ask(Http.Bind(listener = api, interface = host, port = port))
    .mapTo[Http.Event]
    .map {
      case Http.Bound(address) =>
        println(s"REST interface bound to $address")
      case Http.CommandFailed(cmd) =>
        println(s"REST interface could not bind to $host:$port, ${cmd.failureMessage}")
        system.shutdown()
    }
}

