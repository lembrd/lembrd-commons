package com.lembrd.utils

import java.util.concurrent.atomic.AtomicInteger

import com.google.common.util.concurrent.{AbstractIdleService, Service}
import javax.inject.Inject

/**
  *
  * User: lembrd
  * Date: 30/01/17
  * Time: 16:29
  */

trait ServiceBase extends AbstractIdleService {
  val serviceIndex = ServiceCounter.counter.incrementAndGet()

  @Inject
  def setServices( services : Services ): Unit = {
    services.addService(this)
  }

}

class Services {
  val services = new java.util.ArrayList[Service]()

  def addService(s:Service) : Unit = {
    services.add(s)
  }
}

object ServiceCounter {
  val counter = new AtomicInteger()
}