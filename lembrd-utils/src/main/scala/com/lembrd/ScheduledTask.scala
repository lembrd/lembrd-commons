package com.lembrd

import scala.concurrent.Future

/**
  *
  * User: lembrd
  * Date: 21/03/2018
  * Time: 02:03
  */
case class ScheduledTask[X](result: Future[X], id: Long)
