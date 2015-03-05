package com.example

import spray.http._
import spray.routing._
import MediaTypes._
import StatusCodes._

trait Demo1Service extends HttpService {

  val demo1Route = path("") {
    get {
      respondWithMediaType(`text/html`) {
        complete {
          <html>
              <body>
                <h4>Say hello to <i>spray-routing</i> on  <i>spray-can</i>!</h4>
              </body>
          </html>
        }
      }
    }
  } ~ path("foo" ~ Slash.?) {
    get {
      complete("/foo")
    }
  } ~ path("i" ~ IntNumber) { i =>
    get {
      complete(s"IntNumber:$i")
    }
  } ~ path("color" / Map("red" -> 1, "green" -> 2)) { value =>
    get {
      complete(s"$value")
    }
  } ~ path("profile" / "id" ~ Segment) { id =>
    complete(s"$id")
  } ~ pathPrefix("unmatched"){
    unmatchedPath { u =>
      complete(s"$u")
    }
  } ~ pathPrefix("pathEnd"){
        pathEnd {
          complete("/pathEnd")
        } ~
        path("continue"){
          complete("continue")
        }
  }

  val handledRoute = pathPrefix("handled") {
    handleRejections(RejectionHandler {
      case Nil => complete(NotFound,"Oh man, what you are looking for is long gone.")
    }) {
        path("existing"){
          complete("This path exists")
        }
    }

  }

}
