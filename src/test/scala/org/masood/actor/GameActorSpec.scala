package org.masood.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.bot4s.telegram.models.User
import org.masood.actor.WorldActions.AddIndividual
import org.scalatest.Matchers.convertToAnyShouldWrapper
import org.scalatest.{BeforeAndAfterAll, FunSpecLike}

class GameActorSpec extends TestKit(ActorSystem("test-system"))
  with ImplicitSender
  with FunSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  it("should be able to add users") {
    val sender = TestProbe()
    val actor = system.actorOf(Props(new GameActor(new User(0, false, "Zeus"))))
    actor ! AddIndividual(new User(1, false, "Mamali"))
    val state: String = expectMsgType[String]
    state shouldBe "User 'Mamali' added. Population: 1"
    actor ! AddIndividual(new User(2, false, "Moki"))
    val state2: String = expectMsgType[String]
    state2 shouldBe "User 'Moki' added. Population: 2"
  }

}
