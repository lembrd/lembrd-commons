package com.lembrd.akka

import akka.actor.Actor
import akka.actor.Actor.Receive
import com.lembrd.utils.SimpleExecutors

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

/**
  *
  * User: lembrd
  * Date: 06/02/2018
  * Time: 06:20
  */

trait ActorSimpleApi {
  implicit def executionContext: ExecutionContext

  def schedule(delay: FiniteDuration, message: Any): Unit

  def switch[X <: ActorState](rcv: (X) => Receive, state: X): Unit

  def switch[X <: ActorState](x : ActorReceiveWithState[X]) : Unit = {
    switch(x.rcv, x.state)
  }

  def pipeToSelf[X](f: Future[X]): Unit

  def receive: Receive
}

trait ActorState

trait ActorSimpleApiImpl extends Actor with ActorSimpleApi {
  implicit def executionContext: ExecutionContext = SimpleExecutors.misc

  override def schedule(delay: FiniteDuration, message: Any): Unit = context.system.scheduler.scheduleOnce(delay, self, message)

  override def switch[X <: ActorState](rcv: X => Receive, state: X): Unit = {
    context.become(rcv(state))
  }

  override def pipeToSelf[X](f: Future[X]): Unit = akka.pattern.pipe(f).to(self)


}

case class ActorReceiveWithState[X](rcv: X => Receive, state: X)