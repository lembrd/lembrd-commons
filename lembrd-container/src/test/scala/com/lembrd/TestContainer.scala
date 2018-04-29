package com.lembrd

import java.util.concurrent.{Executors, TimeUnit}

import com.google.inject.Guice
import com.lembrd.utils._
import net.codingwell.scalaguice.ScalaModule
import nl.grons.metrics.scala.MetricsSupport

import scala.io.AnsiColor
import scala.util.{Random, Try}

/**
  *
  * User: lembrd
  * Date: 18/02/2018
  * Time: 15:02
  */

object TestContainer extends App {
  val injector = Guice.createInjector(
    new ContainerModule(),
    new ConfigModule[SampleConfig](),
    new ScalaModule {
      override def configure(): Unit = {
//        bind[MetricsPusher].asEagerSingleton()
        bind[TestService].asEagerSingleton()
      }
    }
  )

  injector.getInstance(classOf[GuiceServiceManager]).start()
  injector.getInstance(classOf[TestService]).doTest()

  Thread.sleep(10000)
}


class TestService extends ServiceBase with MetricsSupport with Log {
  MetricsHolder.basePrefix = "jvmapp.testApp"

  val srv = Executors.newScheduledThreadPool(1)

  val _gg = gauge("test_gauge") {
    Random.nextDouble() * 100
  }

  val _meter = meter("test_meter")
  val _counter = counter("test_counter")
  val _histo = histogram("test_histo")


  override def shutDown(): Unit = {
    logger.info("Service stopping")
  }


  override def startUp(): Unit = {

    srv.scheduleWithFixedDelay(new Runnable {
      override def run(): Unit = {
        _counter += 1
        _meter.mark(Random.nextInt(100))
        _histo.+=(Random.nextInt(100))
      }
    }, 1, 1, TimeUnit.SECONDS)

    println("Start---")
    logger.info(s"${AnsiColor.GREEN}Service starting${AnsiColor.RESET}")
  }

  def doTest(): Unit = {
    logger.warn(s"${AnsiColor.RED}Warning${AnsiColor.RESET}")
    logger.error("Error")
    logger.error("Error", new RuntimeException("Failed", new RuntimeException("failedRoot")))

    logger.info(
      s"""
         |Multiline
         |  Message
         |    Test
         |  <-
         |<-
       """.stripMargin)


    println(Try {
      java.net.InetAddress.getLocalHost.getHostName
    }.getOrElse("na")
    )
  }
}


case class SampleConfig(sample : Option[String] = None)