package com.lembrd.utils

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import com.google.common.util.concurrent.Service.State
import com.google.common.util.concurrent.{MoreExecutors, Service, ServiceManager}
import com.google.inject.Injector
import javax.inject.Inject
import org.slf4j.LoggerFactory

import scala.compat.java8.functionConverterImpls.AsJavaConsumer
import scala.concurrent.TimeoutException
import scala.util.Try

/**
  *
  * User: lembrd
  * Date: 30/01/17
  * Time: 16:30
  */
class GuiceServiceManager @Inject()(val injector : Injector, val services : Services) extends Log{
  import collection.convert.wrapAsScala._

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run() {
      GuiceServiceManager.this.stop()
    }
  })

  def start() : Unit = {
    services.services.forEach( new AsJavaConsumer[Service]( srv => srv.addListener( new Service.Listener {
      val log = LoggerFactory.getLogger(srv.getClass)

      override def running(): Unit = {
        log.info("RUNNING")
      }

      override def terminated(from: State): Unit = {
        log.info("TERM(" + from +")")
      }

      override def starting(): Unit = {
        log.info("STARTING")
      }

      override def stopping(from: State): Unit = {
        log.info("STOPPING(" + from +")")
      }

      override def failed(from: State, failure: Throwable): Unit = {
        log.error("FAILED(" + from +")", failure)
      }

    }, MoreExecutors.directExecutor())))

    val serviceManager = new ServiceManager(services.services)

    logger.info("->> Starting services: " + serviceManager.servicesByState().size() )

    services.services.toIndexedSeq.collect{
      case x: ServiceBase => x
    }.sortBy(_.serviceIndex).foreach{ srv =>
      logger.info("Starting: " + srv)
      try {
        srv.startAsync().awaitRunning(3, TimeUnit.MINUTES)
      } catch {
        case th : TimeoutException =>
          logger.error("Service starting timeout: " + srv)
          throw th
        case th:Throwable =>
          logger.error("Service starting failed: " + srv, th)
          throw th
      }
    }


    logger.info(
      s"""
         |
         | TRAVIS : ${ConfigParser.underTravis}
         | CFG    : ${Try{injector.getInstance(classOf[ConfigParser]).confFile}.getOrElse("n/a")}
         |
         |======= Server started ========""".stripMargin)
  }

  val stopHandler : AtomicReference[() => Unit] = new AtomicReference[() => Unit]( this.stopImpl )

  def stop() : Unit = {
    stopHandler.getAndSet( () => Unit ).apply()
  }

  def stopImpl() : Unit = {
    logger.info("STOPPING SERVER")

    services.services.toIndexedSeq.collect{
      case x: ServiceBase => x
    }.sortBy(_.serviceIndex).reverse.foreach{ srv =>
      logger.info("Stopping: " + srv)
      srv.stopAsync()
      srv.awaitTerminated(1, TimeUnit.MINUTES)
    }
  }

}
