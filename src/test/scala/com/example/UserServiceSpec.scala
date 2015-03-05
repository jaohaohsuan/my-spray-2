package com.example

import org.specs2.mutable.Specification
import spray.httpx.Json4sSupport
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import akka.actor._
import org.json4s._
import org.json4s.native.JsonMethods._
import CollectionJsonProtocol.`application/vnd.collection+json`
import spray.httpx.marshalling._

class UserServiceSpec extends Specification with Specs2RouteTest with UserService with Json4sSupport{

  val json4sFormats = org.json4s.DefaultFormats


  def actorRefFactory = system

  override val userAggregateManager = system.actorOf(UserAggregateManager.props)

  "UserService" should {
   
    val unnamedTemplate = """
      {
        "template" : {
          "data" : [
             { "name" : "name", "value" : "", "prompt" : "username" },
             { "name" : "pass", "value" : "?", "prompt" : "password" }
          ]
        }
      }
    """

   implicit val templateMarshaller =
      Marshaller.of[String](`application/vnd.collection+json`) { (value, contentType, ctx) =>
        ctx.marshalTo(HttpEntity(contentType, value))
      }

    "To register without name should not be accepted" in {
      Post("/user",unnamedTemplate) ~> userRoute ~> check {
        status === NotAcceptable
      }
    }

  }
}
