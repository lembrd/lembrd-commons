package lembrd.akka

import akka.PriorityStash
import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout
import com.lembrd.utils._
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ArrayBuffer

/**
  *
  * User: lembrd
  * Date: 08/02/2018
  * Time: 02:35
  */

class PriorityStashTest extends FlatSpec with Matchers  {

  val system = ActorSystem()

  "Priority stash" should "handle stash/unstash with priority" in {
    val a = getActor
    import a._

    actor ! "s1"
    Thread.sleep(100)
    buffer.head shouldBe "s1"

    actor ! "stash"

    actor ! "s2"
    actor ! 1
    actor ! 2
    actor ! 3
    actor ! "s3"

    actor ! "unstash"
    Thread.sleep(100)
    buffer.toIndexedSeq shouldBe Seq("s1", 1, 2, 3, "s2", "s3")
  }


  def getActor = new Object {
    var buffer = ArrayBuffer[Any]()

    val actor = system.actorOf(Props(
      new Actor with PriorityStash {

        def stashed: Receive = {
          case "unstash" =>
            println(s"--> unstash: ${_stashed}")
            context.become(init)
            unstash()

          case x =>
            println(s"STASH: ${x}")
            stash(x, sender())
        }

        def init: Receive = {
          case "stash" =>
            println("-> stashed")
            context.become(stashed)
          case x =>
            println(s"-> handle:${x}")
            buffer.synchronized {
              buffer += x
            }
        }

        override def receive: Receive = init

        override def getPriority(x: Any): Int = {
          x match {
            case _: Int => 100
            case _: String => 10
          }
        }
      }
    ))

  }

  it should "stash during unstash without recursion and keep order" in {
    Thread.sleep(100)
    println("--------")
    val a = getActor
    import a._

    actor ! "stash"
    // inside stash
    actor ! "a"
    actor ! "stash" // stash recursivly
    actor ! "b"
    actor ! "unstash"
    actor ! "unstash"
    Thread.sleep(100)
    buffer.toIndexedSeq shouldBe Seq("a", "b")
  }

  it should "handle real sender during stash" in {
    val actor1 = system.actorOf(Props(new Actor with PriorityStash {

      override def receive: Receive = init

      override def getPriority(x: Any): Int = 1

      def init: Receive = {
        case "stash" =>
          println("stash")
          context.become(stashed)

        case i: Int =>
          println(s"Reply: ${i} to ${sender()}")
          sender ! i
          Thread.sleep(100)
      }

      def stashed: Receive = {
        case "unstash" =>
          println("UNSTASH")
          context.become(init)
          unstash()

        case i: Int =>
          println(s"stash value: ${i}")
          stash(i, sender())
      }

    }),"a1")

    val actor2 = system.actorOf(Props(new Actor {
      override def receive: Receive = {
        case "unstash" =>
          actor1 ! "unstash"

        case x =>
          println(s"Unexpected value received: ${x}")
      }
    }),"a2")

    import akka.pattern._

    import scala.concurrent.duration._
    implicit val timeout = Timeout(1 seconds)

    actor1.ask(1).fResult() shouldBe 1
    actor1 ! "stash"
    new Thread {
      override def run(): Unit = {
        Thread.sleep(500)
        println("UNstashing ->")
        actor2 ! "unstash"
      }
    }.start()
    actor1.ask(2).fResult() shouldBe 2
  }

}
