package lembrd.akka

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, OneForOneStrategy, Props, SupervisorStrategy}

/**
  *
  * User: lembrd
  * Date: 20/03/2018
  * Time: 01:15
  */
class TestSupervisor extends Actor {

  override def supervisorStrategy: SupervisorStrategy = {
    OneForOneStrategy(maxNrOfRetries = -1) {
      case _ =>
        println("Restarting actor...")
        Restart
    }
  }

  override def receive: Receive = {
    case p: Props =>

      sender() ! context.actorOf(p)
  }
}
