package com.example

import org.specs2.mutable.Specification
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.Specs2RouteTest

import scala.concurrent.ExecutionContext

/**
 * Created by henry on 2/26/15.
 */
class SecurityServiceSpec extends Specification with Specs2RouteTest with SecurityService {

  def actorRefFactory = system

  implicit def executionContext: ExecutionContext = system.dispatcher

  "SecurityService" should {

    "return unauthorized" in {
      Get("/secured") ~> sealRoute(securityRoute) ~> check {
        status === Unauthorized
      }
    }

    val henryCred = BasicHttpCredentials("henry", "p1978")

    "return Hi henry" in {
      Get("/secured/") ~>
        addCredentials(henryCred) ~>
        securityRoute ~> check {
          responseAs[String] must contain("henry")
        }
    }

    "return henry can visit grandsys" in {
      Get("/secured/grandsys") ~>
        addCredentials(henryCred) ~>
        securityRoute ~> check {
          status === OK
        }
    }

    val sheenaCred = BasicHttpCredentials("sheena", "home")

    "return others is forbidden" in {
      Get("/secured/grandsys") ~>
        addCredentials(sheenaCred) ~>
        sealRoute(securityRoute) ~> check {
          status === Forbidden
        }
    }
  }
}
