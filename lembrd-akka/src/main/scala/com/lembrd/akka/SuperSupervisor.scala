package com.lembrd.akka

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{OneForOneStrategy, SupervisorStrategyConfigurator}
import com.lembrd.utils.Log

/**
  *
  * User: lembrd
  * Date: 04/02/2018
  * Time: 18:39
  */

case class SuperSupervisor() extends SupervisorStrategyConfigurator with Log {
  logger.debug("Using SuperSupervisor")

  override def create() = {
    OneForOneStrategy(maxNrOfRetries = -1) {
      case x: Exception =>
        logger.error("Error", x)
        Resume
    }
  }
}