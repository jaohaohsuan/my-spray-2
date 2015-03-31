package com.example

/**
 * Created by henry on 3/30/15.
 */

import akka.actor._
import akka.persistence._
import scala.language.postfixOps

object GroupsAggregate {

  case class Join(user: String, groups: Set[String]) extends Command
  case class UserJoined(value: Map[String, Set[String]]) extends Event

  case class Group(userGroups: Map[String, Set[String]]) extends State

  type Users = Set[String]
  type Groups = Set[String]
}

class GroupsAggregate extends PersistentActor with ActorLogging {

  import GroupsAggregate._

  override def persistenceId = "groups-aggregate"

  var state: State = Group(Map())

  def afterEventPersisted(evt: Event): Unit = {
    updateState(evt)
    sender ! state
  }

  def updateState(evt: Event): Unit = evt match {
    case UserJoined(value) =>
      state match {
        case s: Group =>
          state = s.copy(s.userGroups ++ value)
      }
    case _ =>
  }

  val receiveCommand: Receive = {

    case Join(user, groups) =>
      persist(UserJoined(groups.map((_ -> Set(user))) toMap)) { evt =>
        updateState(evt)
      }
    case _ =>
  }

  val receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
    case SnapshotOffer(_, snapshot: State) =>
      state = snapshot
    //context might become different
  }

}