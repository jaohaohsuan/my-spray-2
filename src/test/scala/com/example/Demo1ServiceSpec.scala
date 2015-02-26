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

  }

}