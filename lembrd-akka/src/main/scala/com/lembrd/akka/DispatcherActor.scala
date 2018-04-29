package com.lembrd.akka

import java.util

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorRef, Cancellable, OneForOneStrategy, SupervisorStrategy}
import akka.{XSchedule, XScheduleOnce}
import com.lembrd.utils.{Log, SimpleExecutors}

import scala.compat.java8.functionConverterImpls.{AsJavaBiConsumer, AsJavaConsumer}
import scala.util.control.NonFatal

/**
  *
  * User: lembrd
  * Date: 19/03/2018
  * Time: 21:26
  */

class DispatcherActor extends Actor with Log {

  val subscriptions = new util.HashMap[ActorRef, util.HashSet[ActorRef]]()
  val timers = new util.HashMap[ActorRef, util.HashMap[Any, Cancellable]]()

  override def supervisorStrategy: SupervisorStrategy = {
    OneForOneStrategy(maxNrOfRetries = -1) {
      case x =>
        logger.warn("Restarting actor", x)
        Restart
    }
  }

  override def receive: Receive = {
    case t: CreateRestartableActor =>
      try {
        sender() ! Right(context.actorOf(t.props, t.name))
      } catch {
        case NonFatal(x) =>
          sender() ! Left(x)
      }

    case t: XSchedule =>
      val dispatcher = sender()

      val set = timers.get(dispatcher) match {
        case null =>
          val x = new util.HashMap[Any, Cancellable]()
          timers.put(dispatcher, x)
          x
        case x => x
      }

      if (set.get(t.message) == null) {
        val xx: Cancellable = context.system.scheduler.schedule(t.initialDelay, t.interval, t.receiver, t.message)(SimpleExecutors.misc)
        set.put(t.message, xx)
      }

    case t: XScheduleOnce =>
      context.system.scheduler.scheduleOnce(t.initialDelay, t.receiver, t.message)(SimpleExecutors.misc)
    /*
          val set = timers.get(dispatcher) match {
            case null =>
              val x = new util.HashMap[Any, Cancellable]()
              timers.put(dispatcher, x)
              x
            case x => x
          }

          if (set.get(t.message) == null) {
            val xx: Cancellable = context.system.scheduler.schedule(t.initialDelay, t.interval, t.receiver, t.message)(SimpleExecutors.misc)
            set.put(t.message, xx)
          }
    */

    case XTerminated(actor) =>
//      logger.debug(s"Terminated: ${actor}")
      subscriptions.remove(actor)

      Option(timers.get(actor)).foreach(_.forEach(new AsJavaBiConsumer[Any, Cancellable]((x, y) => {
        y.cancel()
      })))

      timers.remove(actor)

    case XSubscribe(listener) =>
      val dispatcher = sender()
      val set = subscriptions.get(dispatcher) match {
        case null =>
          val x = new util.HashSet[ActorRef]()
          subscriptions.put(dispatcher, x)
          x
        case x => x
      }
      set.add(listener)

    case XBroadcast(msg) =>

      val dispatcher = sender()
      subscriptions.get(dispatcher) match {
        case null =>
        case set =>
          set.forEach(new AsJavaConsumer[ActorRef](c => {
            c.tell(msg, dispatcher)
          }))
      }
  }
}

case class XBroadcast(msg: Any)

case class XTerminated(x: ActorRef)