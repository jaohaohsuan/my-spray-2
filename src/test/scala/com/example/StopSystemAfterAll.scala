package com.example

/**
 * Created by henry on 3/31/15.
 */

import org.scalatest.{ Suite, BeforeAndAfterAll }
import akka.testkit.TestKit

trait StopSystemAfterAll extends BeforeAndAfterAll {
  this: TestKit with Suite =>

  override protected def afterAll(): Unit = {
    super.afterAll()

    system.shutdown()
  }
}
