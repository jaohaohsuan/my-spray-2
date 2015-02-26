package com.example

import spray.http._
import spray.routing._
import MediaTypes._

trait Demo1Service extends HttpService {

  var demo1Route = path("") {
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
  }

}