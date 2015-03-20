package com.example

/**
 * Created by henry on 3/20/15.
 */

import com.typesafe.config.ConfigFactory
import akka.actor.{Props, ActorSystem}

object BackendMain  {

  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.load("backend")
    val system = ActorSystem("backend", config)

    system.actorOf(UserAggregateManager.props, "userAggregateManager")

  }

}
