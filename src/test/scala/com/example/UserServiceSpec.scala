package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import akka.actor._

class UserServiceSpec extends Specification with Specs2RouteTest with UserService {

  val json4sFormats = org.json4s.DefaultFormats

  def actorRefFactory = system

  override val userAggregateManager = system.actorOf(UserAggregateManager.props)

  "UserService" should {
  
    "To register without name should not be accepted" in {
      Post("/user", Map("name" -> "", "pass" -> "?")) ~> userRoute ~> check {
        status === NotAcceptable
      }
    }

  }
}
