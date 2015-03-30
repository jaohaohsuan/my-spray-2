

package com.example

/**
 * Created by henry on 3/19/15.
 */

import com.sksamuel.elastic4s.ElasticClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.specs2.mutable.Specification
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.Specs2RouteTest

class ElasticDemo1ServiceSpec extends Specification with Specs2RouteTest with ElasticDemo1 {

  def actorRefFactory = system

  //override def executionContext = system.dispatcher

  val settings = ImmutableSettings.settingsBuilder()
    .put("http.enabled", false)
    .put("path.home", "/Users/henry/Downloads/elasticsearch-1.4.4/data/local")

  val client = ElasticClient.local(settings.build)

  "ElasticDemo1ServiceSpec" should {
    "index and delete" in {

      Delete("/elastic/programmer/scala/1234") ~> esRoute ~> check {
        handled must beTrue
        //println(responseAs[String])
      }

      Put("/elastic/index") ~> sealRoute(esRoute) ~> check {
        handled must beTrue
        status === OK
        responseAs[String] === "1234"
      }

    }
  }
}
