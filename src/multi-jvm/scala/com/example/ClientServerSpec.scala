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

  }
}
