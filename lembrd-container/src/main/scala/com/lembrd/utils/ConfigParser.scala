package com.lembrd.utils

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import javax.inject.Singleton

/**
  *
  * User: lembrd
  * Date: 30/01/17
  * Time: 16:30
  */

object ConfigParser {
  val ENV_CFG_NAME = "MODULE_CONFIG"

  def prop(s: String): Option[String] = Option(System.getProperty(s)).orElse(Option(System.getenv(s)))

  def getSystemConfig: Option[File] = prop(ENV_CFG_NAME).map(new File(_))

  def underTravis = prop("TRAVIS").exists(_.equalsIgnoreCase("true"))

}

@Singleton
case class ConfigParser(confFile: Option[File],
                        presetConfigs: Option[Config] = None
                       ) extends Log {


  lazy val configStrings: Map[String, String] = Map.empty

  lazy val get: Config = {

    val realConfig = {

      val original = ConfigFactory.load()
      confFile.map { c =>
        if (!c.exists() || !c.canRead) {
          throw new IllegalStateException("Can not read configuration file: " + c.getPath)
        }

        logger.info("Load application configuration from: " + c.getPath)
        val appConf = ConfigFactory.parseFile(c)
        appConf.withFallback(original)

      }.getOrElse(original)
    }

    val fullConfig = presetConfigs.fold(realConfig)(x => x.withFallback(realConfig))

    fullConfig
  }


}
