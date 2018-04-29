package nl.grons.metrics.scala

import java.util.concurrent.atomic.AtomicReference

import com.codahale.metrics.{Metric, MetricFilter, CachedGauge => DropwizardCachedGauge, Gauge => DropwizardGauge, Timer => DropwizardTimer}
import com.lembrd.utils.MetricsHolder
import nl.grons.metrics.scala.MoreImplicits.RichAtomicReference
import org.apache.commons.lang.StringUtils

import scala.concurrent.duration.FiniteDuration

/**
  *
  * User: lembrd
  * Date: 30/01/17
  * Time: 17:14
  */

trait MetricsSupport  {
  protected lazy val metricBaseName = MetricName(s"${MetricsHolder.basePrefix}.${getClass.getSimpleName}")

  protected def metricsRegistry = MetricsHolder.registry
  private[this] val gauges: AtomicReference[Seq[DropwizardGauge[_]]] = new AtomicReference(Seq.empty)


  protected def fromDecimal(d : Int) : Long ={
    d * 1000000000
  }

  /**
    * Registers a new gauge metric.
    *
    * @param name the name of the gauge
    * @param scope the scope of the gauge or null for no scope
    */
  protected def gauge[A](name: String, scope: String = null)(f: => A): Gauge[A] = {
    wrapDwGauge(metricNameFor(name, scope), new DropwizardGauge[A] { def getValue: A = f })
  }


  /**
    * Registers a new gauge metric that caches its value for a given duration.
    *
    * @param name the name of the gauge
    * @param timeout the timeout
    * @param scope the scope of the gauge or null for no scope
    */
  protected def cachedGauge[A](name: String, timeout: FiniteDuration, scope: String = null)(f: => A): Gauge[A] = {
    wrapDwGauge(metricNameFor(name, scope), new DropwizardCachedGauge[A](timeout.length, timeout.unit) { def loadValue: A = f })
  }

  private def wrapDwGauge[A](name: String, handler : =>  DropwizardGauge[A]): Gauge[A] = {
    Option(metricsRegistry.getGauges.get(name)).fold{
      val dwGauge = handler
      metricsRegistry.register(name, dwGauge)
      gauges.getAndTransform(_ :+ dwGauge)
      new Gauge[A](dwGauge)
    }{ dwGauge =>
      new Gauge[A](dwGauge.asInstanceOf[DropwizardGauge[A]])
    }
  }

  /**
    * Creates a new counter metric.
    *
    * @param name the name of the counter
    * @param scope the scope of the counter or null for no scope
    */
  protected def counter(name: String, scope: String = null): Counter = Option(metricsRegistry.getCounters().get(name)).fold{
    new Counter(metricsRegistry.counter(metricNameFor(name, scope)))
  }{ new Counter(_)}


  /**
    * Creates a new histogram metric.
    *
    * @param name the name of the histogram
    * @param scope the scope of the histogram or null for no scope
    */
  protected def histogram(name: String, scope: String = null): Histogram =Option(metricsRegistry.getHistograms.get(name)).fold {
    new Histogram(metricsRegistry.histogram(metricNameFor(name, scope)))
  }{ new Histogram(_)}

  /**
    * Creates a new meter metric.
    *
    * @param name the name of the meter
    * @param scope the scope of the meter or null for no scope
    */
  protected def meter(name: String, scope: String = null): Meter = Option(metricsRegistry.getMeters.get(name)).fold {
    new Meter(metricsRegistry.meter(metricNameFor(name, scope)))
  }{ new Meter(_)}

  /**
    * Creates a new timer metric.
    *
    * @param name the name of the timer
    * @param scope the scope of the timer or null for no scope
    */
  protected def timer(name: String, scope: String = null): TimerExt = Option(metricsRegistry.getTimers.get(name)).fold{
    new TimerExt(metricsRegistry.timer(metricNameFor(name, scope)))
  }{ new TimerExt(_)}


  /**
    * Unregisters all gauges that were created through this builder.
    */
  def unregisterGauges(): Unit = {
    val toUnregister = gauges.getAndTransform(_ => Seq.empty)
    metricsRegistry.removeMatching(new MetricFilter {
      override def matches(name: String, metric: Metric): Boolean =
        metric.isInstanceOf[DropwizardGauge[_]] && toUnregister.contains(metric)
    })
  }

  protected def metricNameFor(name: String, scope: String = null): String = {
    StringUtils.replace(metricBaseName.append(name, scope).name, "$", "")
  }

  /*
    private val delegate = new nl.grons.metrics.scala.InstrumentedBuilder(){
      override val metricRegistry: MetricRegistry = registry
    }



    def cachedGauge[A](name: String, timeout: FiniteDuration, scope: String = null)(f: => A): Gauge[A] = synchronized {
      val a = metricNameFor(name, scope)

      delegate.wrapDwGauge(metricNameFor(name, scope), new DropwizardCachedGauge[A](timeout.length, timeout.unit) { def loadValue: A = f })

      Option(registry.getGauges.get(a)).map(new TimerExt(_)).getOrElse{
        new TimerExt(registry.timer(a))
      }
    }

    def timerMetric(name : String, scope : String = null): TimerExt = synchronized {
      val a = metricNameFor(name, scope)
      Option(registry.getTimers.get(a)).map(new TimerExt(_)).getOrElse{
        new TimerExt(registry.timer(a))
      }
    }
  */





}
class TimerExt( metric: DropwizardTimer ) extends Timer(metric) {

  def timePartialFunction[A,B]( pf : PartialFunction[A,B] ) : PartialFunction[A,B] = {
    new PartialFunction[A,B] {
      override def isDefinedAt(x: A): Boolean = pf.isDefinedAt(x)

      override def apply(v1: A): B =
        time {
          pf.apply(v1)
        }
    }
  }
}
