package com.lembrd.akka

import akka.actor.{Actor, ActorRef}
import org.slf4j.LoggerFactory

/**
  *
  * User: lembrd
  * Date: 02/11/2017
  * Time: 15:10
  */
trait XSubscriber {
  th: Actor =>
  private var subs: Set[ActorRef] = Set.empty

  override def postRestart(reason: Throwable): Unit = {
    LoggerFactory.getLogger(getClass).error("Failed", reason)
    //th.postRestart(reason)
  }

  def subscribe(x: ActorRef): Unit = {
    subs = subs + x
    x ! XSubscribe(self)
  }

  def unsubscribeAll(): Unit = {
    subs.foreach(_ ! XUnSubscribe(self))
    subs = Set.empty
  }
}

case class XSubscribe(x: ActorRef)

case class XUnSubscribe(x: ActorRef)