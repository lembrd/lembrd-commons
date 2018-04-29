package com.lembrd.akka

import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.Slf4jLoggingFilter
import com.google.inject.{Injector, Provider}
import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}

/**
  *
  * User: lembrd
  * Date: 03/11/2017
  * Time: 01:06
  */
@Singleton
class AkkaProvider @Inject()(config: Config, injector: Injector) extends Provider[ActorSystem] {
  require(classOf[Slf4jLoggingFilter] != null)

  lazy val system = {
    val cc = config.getConfig("cephirer")
    val s = ActorSystem("akka", cc)

    // create default dispatcher

    s.actorOf(Props(new DispatcherActor()), "xdispatcher")

    s
  }

  override def get(): ActorSystem = {
    system
  }
}
