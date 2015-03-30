package com.example

import java.net.URI

import akka.pattern.ask
import akka.util.Timeout
import com.example.CollectionJsonProtocol.`application/vnd.collection+json`
import com.example.UserAggregate.User
import com.example.UserAggregateManager.{ GetUser, RegisterUser }
import com.github.t3hnar.bcrypt._
import net.hamnaberg.json.collection.{ Error, JsonCollection }
import org.json4s._
import org.json4s.native.JsonMethods._
import org.specs2.mutable.Specification
import spray.http.HttpHeaders._
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.Specs2RouteTest

import scala.concurrent.Await
import scala.concurrent.duration._

class UserServiceSpec extends Specification with Specs2RouteTest with UserService {

  implicit val timeout = Timeout(2.0 seconds)

  def actorRefFactory = system

  override val userAggregateManager = system.actorOf(UserAggregateManager.props)

  implicit def executionContext = system.dispatcher

  "UserService" should {

    "To register without name should not be accepted" in {

      val unnamedTemplate = """
      {
        "template" : {
          "data" : [
             { "name" : "name", "value" : "", "prompt" : "username" },
             { "name" : "pass", "value" : "?", "prompt" : "password" }
          ]
        }
      } """

      Post("/user", unnamedTemplate).withHeaders(`Remote-Address`("127.0.0.1")) ~> userRoute ~> check {
        handled must beTrue
        mediaType === `application/vnd.collection+json`
        status === NotAcceptable
        val res: String = responseAs[String]
        println(pretty(render(parse(res))))
        res must contain("blank name is not allowed")

//        responseAs[JsonCollection] === JsonCollection(URI.create("http://com.example/user"),
//          Error(title = "/user/error", code = None, message = Some("blank name is not allowed")))

        body === HttpEntity(ContentType(`application/vnd.collection+json`, HttpCharsets.`UTF-8`), res)
      }
    }

    "return password length is too short" in {

      val template = """
        { "template" : {
            "data" : [
              { "name" : "name", "value" : "usr1"}
              { "name" : "pass", "value" : "1234"},
            ]
          }
        }
      """

      Post("/user", template).withHeaders(`Remote-Address`("127.0.0.1")) ~> userRoute ~> check {
        status === NotAcceptable
        responseAs[String] must contain("password length is too short")
      }
    }

    "return with Location Header and Accepted" in {

      val template = """
      {
        "template" : {
          "data" : [
             { "name" : "name", "value" : "mark", "prompt" : "username" },
             { "name" : "pass", "value" : "i912uuUX?", "prompt" : "password" }
          ]
        }
      }"""

      Post("/user", template).withHeaders(`Remote-Address`("127.0.0.1")) ~> userRoute ~> check {
        status === Created
        headers must contain(RawHeader("Location", "/profile/info"))
      }
    }

    "try to change non-exist user should be rejected" in {

      val cred = BasicHttpCredentials("san", "123456")

      val changePassTemplate = """
      {
        "template" : {
          "data" : [
             { "name" : "pass", "value" : "new", "prompt" : "new password" }
          ]
        }
      } """

      Put("/user/password", changePassTemplate) ~>
        addCredentials(cred) ~>
        userRoute ~> check {
          handled must beFalse
        }
    }

    "change user password" in {

      val name = "henry"
      val pass = "origin?"
      val user = createUserInManager(name, pass)

      val changePassTemplate = """
      {
        "template" : {
          "data" : [
             { "name" : "pass", "value" : "newpWd", "prompt" : "new password" }
          ]
        }
      } """

      val cred = BasicHttpCredentials(name, pass)

      Put("/user/password", changePassTemplate) ~>
        addCredentials(cred) ~>
        userRoute ~> check {
          handled must beTrue
          status === OK
          val u = getUserFromManager(name)
          "newpWd".isBcrypted(u.pass) must beTrue
        }
    }

    "register twice error" in {

      val registerUser =
        """
            {
              "template" : {
                        "data" : [
                           { "name" : "name", "value" : "joe", "prompt" : "username" },
                           { "name" : "pass", "value" : "password1", "prompt" : "password" }
                        ]
                      }
            }
            """

      Post("/user", registerUser).withHeaders(`Remote-Address`("127.0.0.1")) ~> userRoute ~> check {
        status === Created
        val user = getUserFromManager("joe")
        user.name === "joe"
      }
      Post("/user", registerUser).withHeaders(`Remote-Address`("127.0.0.1")) ~> userRoute ~> check {
        status === NotAcceptable
        val user = getUserFromManager("joe")
        user.name === "joe"
        val res: String = responseAs[String]
        println(pretty(render(parse(res))))
        res must contain("use another name")

      }
    }

  }

  private def getUserFromManager(name: String) = {
    val f = (userAggregateManager ? GetUser(name)).mapTo[User]
    Await.result(f, 2.0 seconds)
  }

  private def createUserInManager(name: String, pass: String) = {
    val f = (userAggregateManager ? RegisterUser(name, pass)).mapTo[User]
    Await.result(f, 2.0 seconds)
  }
}
