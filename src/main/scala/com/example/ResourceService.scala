package com.example

import java.net.URI

import akka.actor._
import com.example.CollectionJsonProtocol._
import net.hamnaberg.json.collection._
import spray.http.StatusCodes._
import spray.routing._
import spray.routing.authentication.BasicAuth

object ResourceService {

  case class EsQuery(name: String, syntax: String)

}
trait ResourceService extends HttpService with RequestHandlerCreator with CollectionJsonSupport with UserAuthenticator {

  implicit val resourceAggregateManager: ActorRef

  import ResourceAggregateManagerProtocol._
  import ResourceService._
  import ResourceProtocol._

  val resourceRoute = pathPrefix("resources") {

    authenticate(BasicAuth(userAuthenticator _, realm = "personal")) { user =>

      implicit val validUser: Option[UserAggregate.User] = Some(user)

      path(RestPath) { path =>
        get {
          complete(s"$path")
        } ~
          put {
            entity(as[EsQuery]) { data =>
              implicit ctx =>
                val EsQuery(name, syntax) = data
                handle(CreateResource(QuerySyntax(name, syntax), path.toString))
            }
          } ~
          delete {
            complete(OK)
          }
      }
    }

  }

}

