package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

class Demo1ServiceSpec extends Specification with Specs2RouteTest with Demo1Service {
  def actorRefFactory = system

  "Demo1Service" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> demo1Route ~> check {
        responseAs[String] must contain("Say hello")
      }
    }

    "leave GET request to other paths unhandled" in {
      Get("/unknown") ~> demo1Route ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(demo1Route) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }

    "return a path '/foo' or '/foo/'" in {
      Get("/foo") ~> demo1Route ~> check {
        responseAs[String] === "/foo"
      }
      Get("/foo/") ~> demo1Route ~> check {
        responseAs[String] === "/foo"
      }
    }

    "return an IntNumber:50" in {
      Get("/i50") ~> demo1Route ~> check {
        responseAs[String] === "IntNumber:50"
      }
    }

    "matches '/color/red' or '/color/green' and extracts 1 or 2 respectively" in {
      Get("/color/red") ~> demo1Route ~> check {
        responseAs[String] === "1"
      }
      Get("/color/green") ~> demo1Route ~> check {
        responseAs[String] === "2"
      }
    }

    "matches '/profile/id1234' and extracts 1234" in {
      Get("/profile/id1234") ~> demo1Route ~> check {
        responseAs[String] === "1234"
      }
    }

    "matches '/unmatched/wtf...' and extracts /wtf..." in {
      Get("/unmatched/wtf...") ~> demo1Route ~> check {
        responseAs[String] === "/wtf..."
      }
    }

    "path '/pathEnd' already fully matched" in {
      Get("/pathEnd") ~> demo1Route ~> check {
        responseAs[String]  === "/pathEnd"
      }
      Get("/pathEnd/continue") ~> demo1Route ~> check {
        responseAs[String]  === "continue"
      }
      Get("/pathEnd/") ~> demo1Route ~> check {
        handled === false
      }
    }

    "return the path is gone and handle it" in {
      Get("/handled/missing") ~> handledRoute ~> check {
        responseAs[String] must contain("long gone")
      }
      Get("/handled/existing") ~> handledRoute ~> check {
        handled === true
        responseAs[String] === "This path exists"
      }
    }

  }

}