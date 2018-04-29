package com.lembrd.utils

import org.slf4j.LoggerFactory

import scala.util.Random

/**
  *
  * service for generating unique ID numbers at high scale with some simple guarantees
  * id is composed of:
  * time - 41 bits (millisecond precision w/ a custom epoch gives us 69 years)
  * configured machine id - 10 bits - gives us up to 1024 machines
  * sequence number - 12 bits - rolls over every 4096 per machine (with protection to avoid rollover in the same ms)
  *
  * @see https://github.com/twitter/snowflake/tree/scala_28
  *
  */
class MonotonicallyIncreaseGenerator(val workerId  : Int) {
  import GeneratorConsts._

  val twepoch = 1349035200000L
//    1288834974657L
  val log = LoggerFactory.getLogger(getClass)


  var lastTimestamp = -1L
  var sequence: Long = 0L

  // $COVERAGE-OFF$
  if (workerId > maxWorkerId || workerId < 0) {
    throw new IllegalStateException("can not generate workerId: " + workerId)
  }
  // $COVERAGE-ON$

  def nextId(): Long = synchronized {
    nextId(timeGen())
  }

  def nextId(time: Long): Long = synchronized {
    var timestamp = time
    if (timestamp < lastTimestamp) {
      log.error("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp)

      throw new IllegalStateException("Clock moved backwards.  Refusing to generate id for %d milliseconds".format(
        lastTimestamp - timestamp))
    }

    if (lastTimestamp == timestamp) {
      sequence = (sequence + 1) & sequenceMask
      if (sequence == 0) {
        timestamp = tilNextMillis(lastTimestamp)
      }
    } else {
      sequence = 0
    }

    lastTimestamp = timestamp

    ((timestamp - twepoch) << timestampLeftShift) |
      (workerId << workerIdShift) |
      sequence
  }

  protected def tilNextMillis(lastTimestamp: Long): Long = {
    var timestamp = timeGen()
    while (timestamp <= lastTimestamp) {
      // $COVERAGE-OFF$Impossible to reproduce
      timestamp = timeGen()
      // $COVERAGE-ON$
    }
    timestamp
  }

  def fetchDate(id : Long) = {
    (id >> timestampLeftShift) + twepoch
  }

  def minIdForDate(date: Long) : Long = {
    (date - twepoch) << timestampLeftShift
  }

  protected def timeGen(): Long = System.currentTimeMillis()

}

object GeneratorConsts {
  val workerIdBits = 10L
  val maxWorkerId = -1L ^ (-1L << workerIdBits)
  val sequenceBits = 12L

  val workerIdShift = sequenceBits
  val timestampLeftShift = sequenceBits + workerIdBits
  val sequenceMask = -1L ^ (-1L << sequenceBits)

  val tsMask = -1L ^ (1L << timestampLeftShift)

}

object MonotonicallyIncreaseGenerator {

  val instance = new MonotonicallyIncreaseGenerator( Random.nextInt(GeneratorConsts.maxWorkerId.toInt) )
  def nextId(): Long = instance.nextId()
  def nextId(time: Long): Long = instance.nextId(time)

  def fetchDate(id : Long) = instance.fetchDate(id)

  def minIdForDate(date: Long) = instance.minIdForDate(date)

}

