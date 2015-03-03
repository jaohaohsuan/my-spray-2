package com.example

import akka.actor._
import akka.persistence._

trait state
trait Event

object UserAggregate {
  
  case class Initialize(pass: String) extends state
  case class UserInitialized(encryptedPass: String) extends Event

  def props(id: String): Props = Props(new UserAggregate(id))
}

class UserAggregate(id: String) extends PersistentActor {

  import UserAggregate._

  override def persistenceId = id

  val initial: Receive = {
    case Initialize(pass) =>
      //only concern about how to create event, do not write anything after persisted event logic
      val encryptedPass = pass
      persist(UserInitialized(encryptedPass))(afterEventPersisted)
  }

  val created: Receive = {
    case _ => 
  }
  
  def afterEventPersisted(evt: Event): Unit = evt match {
    case UserInitialized(pass) =>
      context.become(created)
  }

  def receiveCommand = {
    case _ => 
  }

  def receiveRecover = {
    case _ => 
  }
}


