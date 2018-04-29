package com.lembrd.utils

import java.lang.management.ManagementFactory
import java.util

import com.codahale.metrics.jvm.{GarbageCollectorMetricSet, MemoryUsageGaugeSet}
import com.codahale.metrics.{Metric, MetricRegistry, MetricSet}
import nl.grons.metrics.scala.{MetricName, MetricsSupport}

import scala.util.Try

/**
  *
  * User: lembrd
  * Date: 30/01/17
  * Time: 17:19
  */

object MetricsHolder extends MetricsSupport {
  var basePrefix : String = "jvmapp"

  var instanceName: String = Try {
    java.net.InetAddress.getLocalHost.getHostName
  }.getOrElse("na")

  var appName : String = "jvmapp"


  val registry = new MetricRegistry()
  val startTime = System.currentTimeMillis()
  val threads = ManagementFactory.getThreadMXBean

  val moduleDefinition: Option[IVersion] = {
    Try {
      import scala.reflect.runtime.universe

      val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
      val module = runtimeMirror.staticModule("com.lembrd.ModuleVersion")
      val obj = runtimeMirror.reflectModule(module)
      val someTrait = obj.instance.asInstanceOf[IVersion]
      someTrait
    }.toOption
  }

//  registry.get
  import scala.collection.JavaConverters._
  import scala.collection.convert.ImplicitConversionsToScala._

  def msWrapper(prefix : String, ms : MetricSet) : MetricSet = {
    new MetricSet {
      override def getMetrics: util.Map[String, Metric] = {
        ms.getMetrics.map{
          case (k,v) => s"${prefix}.${k}" -> v
        }.asJava
      }
    }
  }

  registry.registerAll(msWrapper("jvmapp.mem",new MemoryUsageGaugeSet()))
  registry.registerAll(msWrapper("jvmapp.gc",new GarbageCollectorMetricSet()))


  gauge("uptime") {
    System.currentTimeMillis() - startTime
  }

  gauge("threadsCount") {
    threads.getThreadCount
  }

  gauge("peakThreadsCount") {
    threads.getPeakThreadCount
  }

  gauge("version") {
    moduleDefinition.map(_.version).getOrElse("n/a")
  }

  override lazy val metricBaseName: MetricName = MetricName("jvmapp")
}

trait IVersion {
  def version: String
}