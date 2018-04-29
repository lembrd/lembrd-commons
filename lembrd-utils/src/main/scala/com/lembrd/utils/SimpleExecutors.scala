package com.lembrd.utils

import java.util.concurrent.{ArrayBlockingQueue, ThreadFactory, ThreadPoolExecutor, TimeUnit}

import nl.grons.metrics.scala.MetricsSupport

import scala.concurrent.ExecutionContext

/**
  *
  * User: lembrd
  * Date: 31/01/17
  * Time: 17:45
  */

object SimpleExecutors extends MetricsSupport {

  lazy val persistExecutors = createExecutor(1, 32, 200000, "persister")
  lazy val httpExecutors = createExecutor(32, 256, 20000, "http")
  lazy val fixExecutors = createExecutor(1, 32, 20000, "fix")
  lazy val misc = createExecutor(32, 32, 20000, "misc")


  def createExecutor(min: Int, max: Int, qSz: Int, name: String) = {
    val queue: ArrayBlockingQueue[Runnable] = new ArrayBlockingQueue[Runnable](qSz)
    val executor = new ThreadPoolExecutor(min, max, 1, TimeUnit.MINUTES, queue, NamedThreadFactory(name))

    gauge(name + "_qsz") {
      queue.size()
    }

    ExecutionContext.fromExecutor(executor)
  }


}

case class NamedThreadFactory(name: String) extends ThreadFactory {
  override def newThread(r: Runnable): Thread = {
    new Thread(r, name)
  }
}
