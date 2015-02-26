package com.example

import spray.http.StatusCodes._
import spray.routing._


/**
 * Created by henry on 2/26/15.
 */
trait ExceptionHandlingService extends HttpService {

  var handleArithmeticException = ExceptionHandler {
    case _:ArithmeticException => complete(BadRequest, "divByZero")
  }

  def exceptionHandlingRoute = pathPrefix("exceptions") {
    handleExceptions(handleArithmeticException){
      path("/div/number/0")
      get {
        complete((10/0).toString)
      }
    }
  }

}
