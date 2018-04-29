package com.lembrd

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

import com.lembrd.utils.{Log, NamedThreadFactory}
import javax.inject.Singleton
import nl.grons.metrics.scala.MetricsSupport
import org.joda.time.DateTime

import scala.concurrent.{Future, Promise}
import scala.util.control.NonFatal

/**
  *
  * User: lembrd
  * Date: 22/03/2018
  * Time: 22:38
  */
@Singleton
class SystemScheduler extends Scheduler with MetricsSupport with Log {

  val pool = Executors.newScheduledThreadPool(2, NamedThreadFactory("sys-scheduler"))

  val tasks = new ConcurrentHashMap[Long, ScheduledTaskWrapper[_]]()

  val counter = new AtomicInteger()

  override def schedule[X](time: DateTime)(op: => X): ScheduledTask[X] = {
    val delay = time.getMillis - System.currentTimeMillis()
    scheduleAfterMillis(delay)(op)
  }


  override def scheduleAfterMillis[X](delay: Long)(op: => X): ScheduledTask[X] = {
    if (delay <= 10) {
      ScheduledTask(Future.failed(new TimeoutException(s"delay = ${delay}")), -1)
    } else {
      val id = counter.incrementAndGet()
      val promise = Promise[X]()
      val runnable = new Runnable {
        override def run(): Unit = {
          tasks.remove(id)
          try {
            val result: X = op
            promise.trySuccess(result)

          } catch {
            case NonFatal(th) =>
              promise.tryFailure(th)
          }
        }
      }

      val f = pool.schedule(runnable, delay, TimeUnit.MILLISECONDS)
      val task = ScheduledTaskWrapper(ScheduledTask(promise.future, id), promise, f)
      tasks.put(id, task)
      task.scheduledTask
    }
  }

  override def scheduleInterval(delay: Long, interval: Long)(op: => Unit): Long = {
    if (delay <= 10 || interval <= 0) {
      throw new IllegalArgumentException(s"delay: ${delay}, interval: ${interval}")
    } else {
      val id = counter.incrementAndGet()

      val runnable = new Runnable {
        override def run(): Unit = {
          try {
            op
          } catch {
            case NonFatal(th) =>
              logger.error("Interval task failed", th)
          }
        }
      }

      val f = pool.scheduleWithFixedDelay(runnable, delay, interval, TimeUnit.MILLISECONDS)
      val task = ScheduledTaskWrapper(ScheduledTask(null, id), null, f)
      tasks.put(id, task)
      id
    }
  }

  override def cancel(id: Long): Boolean = {
    tasks.remove(id) match {
      case null => false
      case x => x.s.cancel(false)
    }
  }
}

case class ScheduledTaskWrapper[X](scheduledTask: ScheduledTask[X], promise: Promise[X], s: ScheduledFuture[_])