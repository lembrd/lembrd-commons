package akka

import akka.actor.{Actor, ActorRef}
import com.lembrd.akka.XTerminated
import nl.grons.metrics.scala.MetricsSupport

import scala.concurrent.duration.FiniteDuration

/**
  *
  * User: lembrd
  * Date: 02/11/2017
  * Time: 15:09
  */

trait XActor extends Actor {
  private[akka] lazy val dispatcher = context.actorSelection("/user/xdispatcher")

  override protected[akka] def aroundPostStop(): Unit = {
    dispatcher ! XTerminated(self)
    super.aroundPostStop()
  }

  override protected[akka] def aroundPreRestart(reason: Throwable, message: Option[Any]): Unit = {

    this match {
      case x: MetricsSupport => x.unregisterGauges()
      case _ =>
    }
    super.aroundPreRestart(reason, message)
  }

  def schedule(initialDelay: FiniteDuration,
               interval: FiniteDuration,
               receiver: ActorRef,
               message: Any) = {

    dispatcher ! XSchedule(initialDelay, interval, receiver, message)

  }

  def scheduleOnce(initialDelay: FiniteDuration,
                   receiver: ActorRef,
                   message: Any) = {

    dispatcher ! XScheduleOnce(initialDelay, receiver, message)

  }

}
