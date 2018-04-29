package akka

import akka.actor.{Actor, ActorCell, ActorRef}
import akka.dispatch.Envelope

import scala.collection.mutable.ArrayBuffer

/**
  *
  * User: lembrd
  * Date: 08/02/2018
  * Time: 01:44
  */

trait PriorityStash extends Actor {
  protected var _stashCounter: Int = 0
  protected var _stashed = ArrayBuffer[StashedMessage]()

  def stash(x: Any, sender : ActorRef) = {
    val idx = _stashCounter
    _stashCounter = idx + 1
    println(s"STASHING from : ${sender}")
    _stashed += StashedMessage(x, idx, getPriority(x), sender)
  }

  def getPriority(x: Any): Int

  private def actorCell = context.asInstanceOf[ActorCell]

  def unstash(): Unit = {
    val sorted = _stashed.sorted.toIndexedSeq
    _stashed.clear()

    val last = actorCell.currentMessage
    try {
      sorted.foreach(x => {
        actorCell.invoke(Envelope(x.msg, x.sender))
      })
    } finally {
      actorCell.currentMessage = last
    }
  }

}

case class StashedMessage(msg: Any, index: Int, priority: Int, sender: ActorRef) extends Comparable[StashedMessage] {
  override def compareTo(o: StashedMessage): Int = {
    var r = Integer.compare(o.priority, priority)
    if (r == 0) {
      r = Integer.compare(index, o.index)
    }
    r
  }

  override def toString: String = s"${msg} i:$index p:$priority"
}