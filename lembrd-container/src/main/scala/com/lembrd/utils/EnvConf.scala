package com.lembrd.utils

import java.io.File

/**
  *
  * User: lembrd
  * Date: 02/11/2017
  * Time: 15:40
  */
object EnvConf {
  def apply(): Option[File] = Option(System.getenv("MODULE_CONFIG"))
    .orElse(Option(System.getProperty("MODULE_CONFIG")))
    .map(fn => new File(fn))
}
