package com.lembrd.utils

/**
  *
  * User: lembrd
  * Date: 28/01/2018
  * Time: 14:11
  */

object StandardScaler {

  def fit(data: Iterable[Double]): StandardScaler = {
    val sz: Double = data.size.toDouble
    val mean: Double = data.sum / sz

    val sum2: Double = data.map { d =>
      val f = d - mean
      f * f
    }.sum

    val stdDev: Double = math.sqrt(sum2 / sz)
    StandardScaler(mean, stdDev)

    //    standard_deviation = sqrt( sum( (x - mean)^2 ) / count(x))
  }

}

case class StandardScaler(mean: Double, stdDev: Double) {
  def invert(d: Double) = {
    d * stdDev + mean
  }

  @inline
  def transformOne(d: Double): Double = {
    (d - mean) / stdDev
  }

  def transform(data: Iterable[Double]): Iterable[Double] = {
    data.map(x => transformOne(x))
  }
}