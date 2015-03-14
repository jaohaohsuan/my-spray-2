package com.example

import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import akka.actor._
import akka.io.IO
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http
import spray.io._
import spray.routing.HttpServiceActor
import akka.pattern.ask

import scala.concurrent.duration._

//import org.apache.camel.util.jsse._

class ServiceActor extends HttpServiceActor with Demo1Service with SecurityService with ExceptionHandlingService with UserService {

  def executionContext = context.dispatcher

  val userAggregateManager = context.actorOf(UserAggregateManager.props)

  def receive = runRoute(demo1Route ~ securityRoute ~ exceptionHandlingRoute ~ userRoute)
}

trait SslConfiguration2 {

  // if there is no SSLContext in scope implicitly the HttpServer uses the default SSLContext,
  // since we want non-default settings in this example we make a custom SSLContext available here
  implicit def sslContext: SSLContext = {
    val keyStoreResource = "/ssl-test-keystore.jks"
    val password = "dtsc1234"

    val keyStore = KeyStore.getInstance("jks")
    keyStore.load(getClass.getResourceAsStream(keyStoreResource), password.toCharArray)
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, password.toCharArray)
    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    context
  }

  // if there is no ServerSSLEngineProvider in scope implicitly the HttpServer uses the default one,
  // since we want to explicitly enable cipher suites and protocols we make a custom ServerSSLEngineProvider
  // available here
  implicit def sslEngineProvider: ServerSSLEngineProvider = {
    ServerSSLEngineProvider { engine =>
      //engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
      //engine.setEnabledProtocols(Array("TLSv1"))
      engine
    }
  }

}

object Boot extends App with SslConfiguration2 {

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


