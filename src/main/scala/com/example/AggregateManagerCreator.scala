package com.example

import akka.actor._
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created by henry on 3/20/15.
 */
trait AggregateManagerCreator {
  this: Actor =>

  def createUserAggregateManager: ActorRef =
    context.actorOf(UserAggregateManager.props, "userAggregateManager")

}

object UserAggregateManagerCreator {
  val config = ConfigFactory.load("frontend").getConfig("backend")
  val host = config.getString("host")
  val port = config.getInt("port")
  val protocol = config.getString("protocol")
  val systemName = config.getString("system")
  val actorName = config.getString("actor")

  def createPath =
    s"$protocol://$systemName@$host:$port/$actorName"

}


trait RemoteAggregateManagerCreator extends AggregateManagerCreator {
  this: Actor =>

  override def createUserAggregateManager = {
    import UserAggregateManagerCreator._
    context.actorOf(Props(classOf[RemoteLookup], createPath))
  }
}

class RemoteLookup(path: String) extends Actor with ActorLogging {
  context.setReceiveTimeout(3 seconds)

  def sendIdentifyRequest: Unit = {
    context.actorSelection(path) ! Identify(path)
  }

  def receive = identify

  val identify: Receive = {
    case ActorIdentity(`path`, Some(actor)) =>
      context.setReceiveTimeout(Duration.Undefined)
      log.info("switching to active state.")
      context.become(active(actor))
      context.watch(actor)

    case ActorIdentity(`path`, None) =>
      log.error(s"Remote actor with path $path is not available.")

    case ReceiveTimeout => sendIdentifyRequest
    case msg: Any =>
      log.error(s"Ignoring message $msg, remote actor is not ready yet.")
  }

  def active(actor: ActorRef): Receive = {
    case Terminated(actorRef) =>
      log.info("Actor $actorRef terminated.")
      log.info("switching to identify state")
      context.become(identify)
      context.setReceiveTimeout(3 seconds)
      sendIdentifyRequest
    case msg: Any =>
      actor forward msg
  }
}
