package com.lembrd.utils

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

/**
  *
  * User: lembrd
  * Date: 04/03/2018
  * Time: 13:15
  */

class CycleDoubleBuffer(val tf: Int) {
  val array = new Array[Double](tf)

  var idx: Int = 0

  var _length: Int = 0

  var _sum: Double = 0.0

  def get(offset: Int = 0): Double = {
    array(validIndex(idx - offset - 1))
  }

  def append(d: Double): Unit = {
    _sum += (-array(idx) + d)

    array(idx) = d
    idx += 1
    _length += 1

    if (idx == tf) {
      idx = 0
    }
  }

  def values(offset: Int = tf) = {
    var bb = new ArrayBuffer[Double](tf)
    var i = idx - math.min(offset, length)
    val maxIdx = idx

    while (i < maxIdx) {

      val v = array(validIndex(i))
      i += 1
      bb += v
    }
    bb
  }

  def avg(): Double = {
    _sum / length
    //    avg(tf)
  }

  def sum() : Double = {
    _sum
  }

  @inline def length = math.min(_length, tf)

  def avg(offset: Int): Double = {
    require(offset <= tf)
    require(offset > 0)

    var i = idx - math.min(offset, length)
    val maxIdx = idx

    var sumValue = 0.0
    var count = 0

    while (i < maxIdx) {
      count += 1
      val v = array(validIndex(i))

      sumValue += v
      i += 1
    }

    sumValue / count
  }

  @tailrec
  final def validIndex(idx: Int): Int = {
    if (idx < 0) {
      validIndex(idx + tf)
    } else if (idx >= tf) {
      validIndex(idx - tf)
    } else {
      idx
    }
  }

  def max(offset: Int = tf): Double = {
    require(offset <= tf)
    require(offset > 0)

    var i = idx - math.min(offset, length)
    val maxIdx = idx

    var maxValue = Double.NegativeInfinity

    while (i < maxIdx) {
      val v = array(validIndex(i))
      if (maxValue < v) {
        maxValue = v
      }

      i += 1
    }

    maxValue
  }

  def min(offset: Int = tf): Double = {
    require(offset <= tf)
    require(offset > 0)

    var i = idx - math.min(offset, length)
    val maxIdx = idx

    var minValue = Double.PositiveInfinity

    while (i < maxIdx) {
      val v = array(validIndex(i))
      if (minValue > v) {
        minValue = v
      }

      i += 1
    }

    minValue
  }

}
