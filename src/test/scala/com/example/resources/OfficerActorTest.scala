package com.example.resources

import akka.actor.{ Props, ActorSystem }
import akka.testkit.TestKit
import com.example.StopSystemAfterAll
import com.example.resources.Keeper._
import com.example.resources.Officer.Register
import org.scalatest.{ WordSpecLike, MustMatchers, WordSpec }

/**
 * Created by henry on 3/31/15.
 */
class OfficerActorTest extends TestKit(ActorSystem("testsystem"))
    with WordSpecLike
    with MustMatchers
    with StopSystemAfterAll {

  "A Office Actor" must {
    "receive AccessGained after sending Register" in {
      val officer = system.actorOf(Props[Officer], "officer1")
      officer.tell(Register("bill", "grandsys/", "company/"), testActor)
      expectMsg(AccessGained)
      //fail("not implemented yet")
    }

    "receive ResourceSaved after sending UpdateResource" in {
      val keeper = system.actorOf(Props[Keeper], "keeper1")
      keeper.tell(UpdateResource("bill", "grandsys/rd1/", "company/department/", "scala team"), testActor)
      expectMsg(ResourceSaved)
    }

    "Get ResourceContent" in {
      val keeper = system.actorOf(Props[Keeper], "keeper2")
      keeper.tell(Get("bill", "grandsys/rd1/", "company/department/"), testActor)
      expectMsg(ResourceContent("scala team"))
    }


    "receive AccessDenied if It's not bill" in {
      val keeper = system.actorOf(Props[Keeper], "keeper3")
      keeper.tell(UpdateResource("henry", "grandsys/rd1/", "company/department/", "scala team"), testActor)
      expectMsg(AccessDenied)
    }

    "receive NotFound if path is incorrect" in {
      val keeper = system.actorOf(Props[Keeper], "keeper4")
      keeper.tell(Get("bill", "grandsys/rd2/", "company/department/"), testActor)
      expectMsg(NotFound)
    }
  }
}
