package lembrd.akka

import akka.XDispatcher
import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorRef, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy}
import akka.testkit.{ImplicitSender, TestKit}
import com.google.inject.{Guice, Injector}
import com.lembrd.akka.{AkkaProvider, RestartableActor, XProvider, XSubscribe}
import com.lembrd.utils.{Log, SimpleExecutors}
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpecLike, Matchers}

/**
  *
  * User: lembrd
  * Date: 19/03/2018
  * Time: 21:36
  */

object TestActorWithSubscriptionRestartSupport {
  SimpleExecutors.misc
  val injector: Injector = Guice.createInjector()
  val provider = new AkkaProvider(ConfigFactory.load(), injector)
  val system = provider.get()
}

class TestActorWithSubscriptionRestart extends TestKit(TestActorWithSubscriptionRestartSupport.system)
  with ImplicitSender
  with FlatSpecLike
  with Matchers {

  "default akka" should "not restart actor" in {
    val a = system.actorOf(Props(new TestActor1))
    a ! XSubscribe(self)

    a ! "1"
    expectMsg(1)
    a ! "2"
    expectMsg(2)
    a ! "3"
    expectMsg(3)
    a ! "restart"
    a ! "1"
    expectMsg(4)
  }

  "akka" should "restart actor but keep subscriptions with xprovider" in {

    val xp = new XProvider[TestActor1]("test")
    xp.injector = TestActorWithSubscriptionRestartSupport.injector
    xp.actorSystem = system
    val a = xp.get()

    a ! XSubscribe(self)

    a ! "1"
    expectMsg(1)
    a ! "2"
    expectMsg(2)
    a ! "3"
    expectMsg(3)
    a ! "restart"
    a ! "1"
    expectMsg(1)
  }

  "akka" should "restart actor but keep subscriptions with supervisor" in {
    val supervisor = system.actorOf(Props(new TestSupervisor))
    supervisor ! Props(new TestActor1())
    val a = expectMsgType[ActorRef]

    a ! XSubscribe(self)

    a ! "1"
    expectMsg(1)
    a ! "2"
    expectMsg(2)
    a ! "3"
    expectMsg(3)
    a ! "restart"
    a ! "1"
    expectMsg(1)

    a ! PoisonPill
  }
}


class TestActor1 extends Actor with Log with XDispatcher with RestartableActor {
  var id: Int = 0

  override def receive: Receive = {
    case "restart" =>
      println("--> throw exception")
      throw new RuntimeException("test exception")

    case x =>
      id += 1
      println(s"Got: ${x} send :${id}")
      broadcast(id)
  }

}

