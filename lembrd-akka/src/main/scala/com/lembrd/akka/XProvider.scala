package com.lembrd.akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import com.google.inject.{Injector, Provider}
import com.lembrd.utils.SimpleExecutors
import javax.inject.Inject

import scala.concurrent.Await
import scala.reflect.ClassTag

/**
  *
  * User: lembrd
  * Date: 04/02/2018
  * Time: 18:38
  */

case class XProvider[T <: Actor : ClassTag](name: String) extends Provider[ActorRef] {
  @Inject()
  var injector: Injector = _

  @Inject()
  var actorSystem: ActorSystem = _

  val cl = implicitly[ClassTag[T]].runtimeClass

  import akka.pattern.ask

  import scala.concurrent.duration._

  implicit val timeout = Timeout(1 minute)
  implicit val executions = SimpleExecutors.misc

  lazy val actor: ActorRef = {
    val props = Props(injector.getInstance(cl.asInstanceOf[Class[_ <: Actor]]))
    val restartable = classOf[RestartableActor].isAssignableFrom(cl)

    if (restartable) {
      println(s"Restartable actor: ${name}")
      val selection = actorSystem.actorSelection("/user/xdispatcher")
      val res = Await.result(selection.ask(CreateRestartableActor(props, name)).collect {
        case Right(x: ActorRef) => x
        case Left(x: Throwable) =>
          throw x
        //case Failed
      }, 1.minute)

      res
    } else {
      actorSystem.actorOf(props, name)
    }
  }

  override def get(): ActorRef = {
    actor
  }
}

trait RestartableActor {
  self: Actor =>
}

case class CreateRestartableActor(props: Props, name: String)