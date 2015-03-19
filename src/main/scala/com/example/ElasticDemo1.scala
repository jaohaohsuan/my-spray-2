package com.example

/**
 * Created by henry on 3/19/15.
 */

import spray.http.StatusCodes._
import spray.routing._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import scala.language.postfixOps

trait elastic {

  import com.sksamuel.elastic4s.ElasticClient
  import com.sksamuel.elastic4s.ElasticDsl._

  def client: ElasticClient

  def idx() = {
    client.execute {
      index into "programmers" -> "scala" fields (
        "name" -> "henry") id 1234
    }
  }

  def del(index: String, `type`: String, id: String) = {
    client.execute {
      delete id id from index -> `type`
    }
  }
}

trait ElasticDemo1 extends HttpService with elastic {

  implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val esRoute = pathPrefix("elastic") {

    path("index") {

      put {
        onComplete(idx) {
          case Success(indexResponse) if indexResponse.isCreated =>
            complete(OK, indexResponse.getId)
          case Failure(ex) =>
            complete(InternalServerError, ex.getMessage)
        }
      }

    } ~
      path(Segment / Segment / Segment) { (index, `type`, id) =>

        delete {
          onComplete(del(index, `type`, id)) {
            case Success(delRes) =>
              complete(OK)
            case Failure(ex) =>
              complete(InternalServerError, ex.getMessage)
          }
        }

      }
  }
}