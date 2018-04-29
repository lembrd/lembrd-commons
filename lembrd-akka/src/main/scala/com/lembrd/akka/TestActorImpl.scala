package com.lembrd.akka

import java.util.function.Supplier

import akka.actor.Actor.Receive
import akka.actor.Status
import com.lembrd.utils.{Log, SimpleExecutors}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  *
  * User: lembrd
  * Date: 06/02/2018
  * Time: 06:38
  */
trait TestActorImpl extends ActorSimpleApi with Log{
  
  var $_STATE: ActorState = EmptyActorState
  var $_Scheduled: Seq[(Any, FiniteDuration)] = Nil

  var $_Receive: Receive = receive

  override implicit def executionContext: ExecutionContext = SimpleExecutors.misc

  override def schedule(delay: FiniteDuration, message: Any): Unit = {
    logger.debug(s"Schedule: ${delay} ${message}")
    $_Scheduled = $_Scheduled :+ (message -> delay)
  }

  import scala.concurrent.duration._

  type F = () => Unit
  val th: ThreadLocal[Seq[F]] = ThreadLocal.withInitial(new Supplier[Seq[F]] {
    override def get(): Seq[F] = Nil
  })

  override def switch[X <: ActorState](rcv: X => Receive, state: X): Unit = {
    logger.debug(s"state : ${$_STATE} => ${state}")
    $_STATE = state
    $_Receive = rcv(state)
  }

  override def pipeToSelf[X](f: Future[X]): Unit = {
    val fun: F = () => {
      try {
        val result = Await.result(f, 1 minute)
        exec(result)
      } catch {
        case th: Throwable =>
          exec(Status.Failure(th))
      }
    }

    th.set(th.get() :+ fun)
  }

  def exec(msg: Any): Boolean = synchronized {

    if ($_Receive.isDefinedAt(msg)) {
      val old = th.get()
      try {
        th.remove()

        logger.debug(s"Handle :${msg} in ${$_STATE}")
        $_Receive.apply(msg)

        th.get().foreach(x => x())
      } finally {
        th.set(old)
      }

      true
    } else {
      logger.debug(s"UNHANDLED :${msg} in ${$_STATE}")
      false
    }
  }

  def !(msg: Any) = exec(msg)


}

case object EmptyActorState extends ActorState