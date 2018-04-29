package com.lembrd

import java.util.Collections
import java.util.concurrent.{Executors, TimeUnit}

import com.lembrd.utils.{Log, MetricsHolder, ServiceBase}
import com.typesafe.config.Config
import configs.Result
import io.prometheus.client.dropwizard.DropwizardExports
import io.prometheus.client.exporter.PushGateway
import javax.inject.Inject

/**
  *
  * User: lembrd
  * Date: 20/02/2018
  * Time: 03:22
  */

case class MetricsConfig(
                          host: String,
                          appName: Option[String],
                          instanceName :Option[String],
                          pushInterval: Int = 10
                        )

class MetricsPusher @Inject()(config: Config) extends ServiceBase with Log {

  val srv = Executors.newScheduledThreadPool(1)

  var conf: Option[MetricsConfig] = {
    if (config.hasPath("cephirer.metrics.host")) {

      import configs.syntax._
      val result = config.get[MetricsConfig]("cephirer.metrics")

      result match {
        case Result.Failure(error) =>
          logger.warn("Can not parse config:\n" +
            error.messages.mkString("\n"))

          None
        case Result.Success(c) =>
          c.appName.foreach { name => MetricsHolder.appName = name }
          c.instanceName.foreach{ name => MetricsHolder.instanceName = name}
          Some(c)
      }


    } else {
      None
    }

  }

  override def shutDown(): Unit = {
    srv.shutdownNow()
  }

  def start(c: MetricsConfig) = {
    import io.prometheus.client.CollectorRegistry
    val registry = new CollectorRegistry

    logger.info(s"Starting metrics pusher with jobId: ${MetricsHolder.appName}, instance: ${MetricsHolder.instanceName}")

    new DropwizardExports(MetricsHolder.registry).register(registry)

    val gw = new PushGateway(c.host)

    srv.scheduleWithFixedDelay(new Runnable {
      override def run(): Unit = {
        try {
          gw.pushAdd(registry, MetricsHolder.appName, Collections.singletonMap("instance", MetricsHolder.instanceName))
        } catch {
          case x: Exception => logger.error("Failed", x)
        }
      }
    }, c.pushInterval, c.pushInterval, TimeUnit.SECONDS)
  }

  override def startUp(): Unit = {
    conf.foreach { c => start(c) }
  }
}
