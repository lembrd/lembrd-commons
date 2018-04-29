package com.lembrd.utils


import net.codingwell.scalaguice.ScalaModule

/**
  *
  * User: lembrd
  * Date: 30/01/17
  * Time: 16:39
  */

class ContainerModule extends ScalaModule {

  override def configure(): Unit = {
    bind[Services].asEagerSingleton()
    bind[GuiceServiceManager].asEagerSingleton()
  }

}

/*

class DatasourceModule extends ScalaModule {
  override def configure(): Unit = {
    bind[ModuleDatasource].asEagerSingleton()
  }
}
*/
