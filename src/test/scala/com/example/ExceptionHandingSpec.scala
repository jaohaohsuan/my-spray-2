package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http.StatusCodes._

/**
 * Created by henry on 2/26/15.
 */
class ExceptionHandingSpec extends Specification with Specs2RouteTest with ExceptionHandlingService {

  def actorRefFactory = system

  "ExceptionHandlingService" should {

    "return divide zero exception" in {
      Get("/exceptions/div/number/0") ~> exceptionHandlingRoute ~> check {
        status === BadRequest
        responseAs[String] === "divByZero"
      }
    }
  }
}
