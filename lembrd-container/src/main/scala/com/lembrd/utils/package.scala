package com.lembrd

import scala.concurrent.{Await, Future}

/**
  *
  * User: lembrd
  * Date: 29/04/2018
  * Time: 14:57
  */
package object utils {
  implicit class RichFuture[X](f: Future[X]) {

    import scala.concurrent.duration._

    def fReady(m: Long = 60000): Unit =
      Await.ready(f, m.millis)

    def fResult(m: Long = 60000): X =
      Await.result(f, m.millis)


  }

}
