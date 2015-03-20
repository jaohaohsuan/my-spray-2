package com.example

/**
 * Created by henry on 3/20/15.
 */

import akka.io.IO
import akka.util.Timeout
import spray.routing.HttpServiceActor
import scala.concurrent.duration._
import scala.language.postfixOps
import com.typesafe.config.ConfigFactory
import akka.actor.{Props, ActorSystem}
import spray.can.Http
import spray.can.Http.Bind
import akka.pattern.ask

object FrontendMain extends SslConfiguration {

  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.load("frontend")

    val host = "localhost"
    val port = 1978

    class FrontendRestApi extends ServiceActor with RemoteAggregateManagerCreator

    implicit val system = ActorSystem("frontend", config)

    val api = system.actorOf(Props[FrontendRestApi], name = "RestInterface")

    implicit val executionContext = system.dispatcher
    implicit val timeout = Timeout(10 seconds)

    Http(system).manager.ask(Bind(listener = api, interface = host, port = port))
      .mapTo[Http.Event]
      .map {
      case Http.Bound(address) =>
        println(s"REST interface bound to $address")
      case Http.CommandFailed(cmd) =>
        println(s"REST interface could not bind to $host:$port, ${cmd.failureMessage}")
        system.shutdown()
    }
  }


}
