package com.example

/**
 * Created by henry on 3/21/15.
 */

import akka.remote.testkit.MultiNodeSpec
import akka.actor._

import akka.testkit.ImplicitSender

class ClientServerSpecMultiJvmFrontend extends ClientServerSpec
class ClientServerSpecMultiJvmBackend extends ClientServerSpec


class ClientServerSpec extends MultiNodeSpec(ClientServerConfig)
  with STMultiNodeSpec
  with ImplicitSender {

  import ClientServerConfig._


  def initialParticipants = roles.size

  "A Client Server configured app" must {
    "wait for nodes to enter a barrier" in {
      enterBarrier("startup")
    }

    "be able to create an event and sell a ticket" in {

      runOn(frontend) {
        enterBarrier("deployed")

      }

      runOn(backend) {
        enterBarrier("deployed")
      }

      enterBarrier("finished")
    }
  }
}
