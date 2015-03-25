package com.example

import java.net.URI

import akka.actor._
import com.example.CollectionJsonProtocol._
import net.hamnaberg.json.collection._
import spray.http.StatusCodes._
import spray.routing._
import spray.routing.authentication.BasicAuth

trait ResourceService extends HttpService with RequestHandlerCreator with CollectionJsonSupport with UserAuthenticator {

  implicit val resourceAggregateManager: ActorRef

  import ResourceAggregateManagerProtocol._

  val resourceRoute = pathPrefix("resources") {

    authenticate(BasicAuth(userAuthenticator _, realm = "personal")) { implicit user =>

      path(RestPath) { path =>
        get {
          complete(s"$path")
        } ~
          put {
            entity(as[CreateResource]) { command =>
              implicit ctx =>
                handle(command)
            }
          } ~
          delete {
            complete(OK)
          }
      }
    }

  }

}

