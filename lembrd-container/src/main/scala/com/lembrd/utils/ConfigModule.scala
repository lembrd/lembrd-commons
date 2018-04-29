package com.lembrd.utils

import java.io.File

import com.google.inject.AbstractModule
import com.typesafe.config.Config
import configs.{Configs, Result}
import net.codingwell.scalaguice.ScalaModule


/**
  *
  * User: lembrd
  * Date: 13/01/2018
  * Time: 19:30
  */

class ConfigModule[X](confFile: Option[File] = EnvConf(),
                               presetConfigs: Option[Config] = None,
                               rootName: String = "cephirer"
                              )(implicit X: Configs[X], manifest:Manifest[X]) extends AbstractModule with ScalaModule {

  lazy val parser: ConfigParser = new ConfigParser(confFile, presetConfigs)
  lazy val config: Config = parser.get

  lazy val conf: X = {
    import configs.syntax._


    val result = config.get[X](rootName)

    result match {
      case Result.Failure(error) =>
        throw new RuntimeException("Can not parse config:\n" +
          error.messages.mkString("\n")) /* +
          "Config:\n" +
          config.root().render())*/
      case Result.Success(c) =>
        c
    }

  }

  override def configure(): Unit = {
    bind[Config].toInstance(config)
    bind[ConfigParser].toInstance(parser)
    bind[X].toInstance(conf)

  }
}
