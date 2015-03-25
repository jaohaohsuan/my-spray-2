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

  def createResourceAggregateManager: ActorRef =
    context.actorOf(Props[UserAggregateManager], "resourceAggregateManager")

}

trait ConfiguredRemoteAggregateManagerDeployment extends AggregateManagerCreator { this: Actor =>
  override def createUserAggregateManager =
    context.actorOf(Props(classOf[RemoteActorForwarder], UserAggregateManager.props, "userAggregateManager"), "forwarder1")

  override def createResourceAggregateManager =
    context.actorOf(Props(classOf[RemoteActorForwarder], Props[ResourceAggregateManager], "resourceAggregateManager"), "forwarder2")
}

class RemoteActorForwarder(props: Props, name: String) extends Actor with ActorLogging {
  context.setReceiveTimeout(10 seconds)

  deployAndWatch()

  def deployAndWatch(): Unit =
    {
      val actor = context.actorOf(props, name)
      context.watch(actor)
      log.info("switching to maybe active state.")
      context.become(maybeActive(actor))
      context.setReceiveTimeout(Duration.Undefined)
    }

  def receive = deploying

  def deploying: Receive = {

    case ReceiveTimeout =>
      deployAndWatch()
    case msg: Any =>
      log.error(s"Ignoring message $msg, not ready yet.")
  }

  def maybeActive(actor: ActorRef): Receive = {

    case Terminated(actorRef) =>
      log.info(s"Actor $actorRef terminated.")
      log.info("switching to deploying state.")
      context.become(deploying)
      context.setReceiveTimeout(10 seconds)

      deployAndWatch()
    case msg: Any => actor forward msg
  }
}

