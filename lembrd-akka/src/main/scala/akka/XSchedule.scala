package akka

import akka.actor.ActorRef

import scala.concurrent.duration.FiniteDuration

/**
  *
  * User: lembrd
  * Date: 19/03/2018
  * Time: 22:31
  */

case class XSchedule(initialDelay: FiniteDuration, interval: FiniteDuration, receiver: ActorRef, message: Any)

case class XScheduleOnce(initialDelay: FiniteDuration, receiver: ActorRef, message: Any)
