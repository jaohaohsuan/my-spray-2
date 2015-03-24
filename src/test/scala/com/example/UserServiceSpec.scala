package com.example

import java.net.URI

import akka.pattern.ask
import akka.util.Timeout
import com.example.CollectionJsonProtocol.`application/vnd.collection+json`
import com.example.UserAggregate.User
import com.example.UserAggregateManager.{GetUser, RegisterUser}
import com.github.t3hnar.bcrypt._
import net.hamnaberg.json.collection.{Error, JsonCollection}
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

      Post("/user", unnamedTemplate) ~> userRoute ~> check {
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

      Post("/user", template) ~> userRoute ~> check {
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

      Post("/user", template) ~> userRoute ~> check {
        status === Accepted
        headers must contain(RawHeader("Location", "/user/user-mark"))
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

      Post("/user", registerUser) ~> userRoute ~> check {
        status === Accepted
        val user = getUserFromManager("joe")
        user.id === "user-joe"
      }
      Post("/user", registerUser) ~> userRoute ~> check {
        status === NotAcceptable
        val user = getUserFromManager("joe")
        user.id === "user-joe"
        val res: String = responseAs[String]
        println(pretty(render(parse(res))))
        res must contain("User has been initialized.")

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
