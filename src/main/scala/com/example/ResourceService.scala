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

  import ResourceService._
  import ResourceProtocol._

  val resourceRoute = pathPrefix("resources") {

    authenticate(BasicAuth(userAuthenticator _, realm = "personal")) { implicit user =>

      path(Segments) { path =>
        get {
          complete(s"$path")
        } ~
          put {
            import ResourceAggregateManager._
            entity(as[EsQuery]) { data =>
              implicit ctx =>
                handle(CreateResource(path, data))
            }
          } ~
          delete {
            complete(OK)
          }
      }
    }

  }

}

