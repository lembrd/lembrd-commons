package com.lembrd

import org.joda.time.DateTime


/**
  *
  * User: lembrd
  * Date: 21/03/2018
  * Time: 00:30
  */

trait Scheduler {

  def schedule[X](time: DateTime)(op: => X): ScheduledTask[X]

  def scheduleAfterMillis[X](millis: Long)(op: => X): ScheduledTask[X]

  def scheduleInterval(delay: Long, interval: Long)(op: => Unit): Long

  def cancel(id : Long) : Boolean
}
