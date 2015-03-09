package com.example

import java.net.URI

import com.example.CollectionJsonProtocol.`application/vnd.collection+json`
import net.hamnaberg.json.collection.{Error, JsonCollection}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.specs2.mutable.Specification
import spray.http.HttpHeaders._
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.Specs2RouteTest

class UserServiceSpec extends Specification with Specs2RouteTest with UserService {

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

    "To register without name should not be accepted" in {
      Post("/user",unnamedTemplate) ~> userRoute ~> check {
        handled must beTrue
        mediaType === `application/vnd.collection+json`
        status === NotAcceptable
        val res: String = responseAs[String]
        println(pretty(render(parse(res))))
        res must contain("You can not register without name.")

        responseAs[JsonCollection] === JsonCollection(URI.create("http://com.example/user"),
                        Error(title = "RegisterUser", code = None, message = Some("You can not register without name.")))

        body === HttpEntity(ContentType(`application/vnd.collection+json`, HttpCharsets.`UTF-8`), res)
      }
    }

    val henryTemplate = """
      {
        "template" : {
          "data" : [
             { "name" : "name", "value" : "henry", "prompt" : "username" },
             { "name" : "pass", "value" : "?", "prompt" : "password" }
          ]
        }
      }
    """

    "return with Location Header and Accepted" in {
      Post("/user", henryTemplate) ~> userRoute ~> check {
        status === Accepted
        headers must contain(RawHeader("Location","/user/henry"))
      }
    }
  }
}
