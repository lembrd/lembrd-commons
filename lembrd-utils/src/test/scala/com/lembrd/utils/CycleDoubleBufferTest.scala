package com.lembrd.utils

import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random

/**
  *
  * User: lembrd
  * Date: 29/04/2018
  * Time: 15:00
  */

class CycleDoubleBufferTest extends FlatSpec with Matchers {
  "simple sma" should "work" in {
    def test(_a: Seq[Double]): Unit = {
      {
        val a = _a.takeRight(24)

        val sma = new CycleDoubleBuffer(24)
        _a.foreach(sma.append)

        val real = a.sum / a.size
        val ss = sma.avg()
        real shouldBe ss +- 0.0000000001
      };

      {
        val a = _a.takeRight(8)

        val sma = new CycleDoubleBuffer(24)
        a.foreach(sma.append)

        val real = a.sum / a.size
        val ss = sma.avg(8)
        real shouldBe ss
      };

      {
        val single = new CycleDoubleBuffer(24)
        single.append(_a.head)
        single.avg() shouldBe _a.head
        single.avg(1) shouldBe _a.head
      }

    }

    test((0).until(32).map(_ => Random.nextDouble()))

    test((0).until(16).map(_ => Random.nextDouble()))

    test((0).until(23).map(_ => Random.nextDouble()))
    test((0).until(24).map(_ => Random.nextDouble()))
    test((0).until(25).map(_ => Random.nextDouble()))
    test((0).until(48).map(_ => Random.nextDouble()))

    test((0).until(256).map(_ => Random.nextDouble()))

  }

  "values" should "work" in {
    def test(_a: Seq[Double]): Unit = {
      {
        val a = _a.takeRight(24)
        val sma = new CycleDoubleBuffer(24)
        _a.foreach(sma.append)

        //        val real = a.max
        val ss = sma.values(24)
        ss shouldBe a
      };
      {
        val a = _a.takeRight(8)
        val sma = new CycleDoubleBuffer(24)
        a.foreach(sma.append)

        val ss = sma.values(8)
        a shouldBe ss
      };

    }

    test((0).until(32).map(_ => Random.nextDouble()))

    test((0).until(16).map(_ => Random.nextDouble()))

    test((0).until(23).map(_ => Random.nextDouble()))
    test((0).until(24).map(_ => Random.nextDouble()))
    test((0).until(25).map(_ => Random.nextDouble()))
    test((0).until(48).map(_ => Random.nextDouble()))

    test((0).until(256).map(_ => Random.nextDouble()))


  }

  "max" should "work" in {
    def test(_a: Seq[Double]): Unit = {
      {
        val a = _a.takeRight(24)
        val sma = new CycleDoubleBuffer(24)
        _a.foreach(sma.append)

        val real = a.max
        val ss = sma.max()
        real shouldBe ss
      };
      {
        val a = _a.takeRight(8)
        val sma = new CycleDoubleBuffer(24)
        a.foreach(sma.append)

        val real = a.max
        val ss = sma.max(8)
        real shouldBe ss
      };

    }

    test((0).until(32).map(_ => Random.nextDouble()))

    test((0).until(16).map(_ => Random.nextDouble()))

    test((0).until(23).map(_ => Random.nextDouble()))
    test((0).until(24).map(_ => Random.nextDouble()))
    test((0).until(25).map(_ => Random.nextDouble()))
    test((0).until(48).map(_ => Random.nextDouble()))

    test((0).until(256).map(_ => Random.nextDouble()))

  }

  "min" should "work" in {
    def test(_a: Seq[Double]): Unit = {
      {
        val a = _a.takeRight(24)
        val sma = new CycleDoubleBuffer(24)
        _a.foreach(sma.append)

        val real = a.min
        val ss = sma.min()
        real shouldBe ss
      }

      {
        val a = _a.takeRight(8)
        val sma = new CycleDoubleBuffer(24)
        a.foreach(sma.append)

        val real = a.min
        val ss = sma.min(8)
        real shouldBe ss
      };

    }

    test((0).until(32).map(_ => Random.nextDouble()))

    test((0).until(16).map(_ => Random.nextDouble()))

    test((0).until(23).map(_ => Random.nextDouble()))
    test((0).until(24).map(_ => Random.nextDouble()))
    test((0).until(25).map(_ => Random.nextDouble()))
    test((0).until(48).map(_ => Random.nextDouble()))

    test((0).until(256).map(_ => Random.nextDouble()))

  }

  "getter" should "work" in {
    val buff = new CycleDoubleBuffer(6)
    Seq(1, 2, 3, 4, 5, 6).foreach(x => buff.append(x))
    buff.get(0) shouldBe 6
    buff.get(1) shouldBe 5
    buff.get(5) shouldBe 1
  }


}
