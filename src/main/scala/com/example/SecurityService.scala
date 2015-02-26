package com.example

import spray.routing.HttpService
import spray.routing.authentication._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/**
 * Created by henry on 2/26/15.
 */
trait SecurityService extends HttpService {

  implicit def executionContext : ExecutionContext

  def myUserPassAuthenticator(userPass: Option[UserPass]): Future[Option[String]] =
    Future {
      userPass match {
        case  Some(UserPass("henry", "p1978")) => Some("henry")
        case  Some(UserPass("sheena", "home")) => Some("sheena")
        case  _ => None
      }
    }

  def securityRoute = pathPrefix("secured") {
    authenticate(BasicAuth(myUserPassAuthenticator _, realm = "secure site")) { username =>
      pathSingleSlash {
        complete(s"Hi $username, it's safe here.")
      }~
      path("grandsys") {
        authorize(username == "henry") {
          complete("only henry can access here")
        }
      }
    }
  }

}
