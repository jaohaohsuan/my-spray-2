package com.example

/**
 * Created by henry on 3/21/15.
 */

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import org.scalatest.MustMatchers

trait STMultiNodeSpec extends MultiNodeSpecCallbacks with WordSpecLike with MustMatchers with BeforeAndAfterAll {

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()

}
