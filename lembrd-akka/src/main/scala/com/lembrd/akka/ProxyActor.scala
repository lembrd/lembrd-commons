package com.lembrd.akka

import akka.actor.{Actor, ActorRef, PoisonPill}

/**
  *
  * User: lembrd
  * Date: 08/02/2018
  * Time: 00:24
  */
case class ProxyActor(originalSender: ActorRef, originalExecutor: ActorRef, message: Any) extends Actor {

  override def preStart(): Unit = {
    originalExecutor ! message
  }

  override def receive: Receive = {
    case x =>
      originalSender ! x
      self ! PoisonPill
  }
}
