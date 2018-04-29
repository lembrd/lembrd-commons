package akka

import com.lembrd.akka.{XBroadcast, XSubscribe, XUnSubscribe}


trait XDispatcher extends XActor {

  override protected[akka] def aroundReceive(receive: Receive, msg: Any): Unit = {
    val newReceive = subscribeReceiveImpl.orElse(receive)
    super.aroundReceive(newReceive, msg)
  }

  @deprecated
  def subscribeReceive: Receive = PartialFunction.empty

  private def subscribeReceiveImpl: Receive = {
    case msg: XSubscribe =>
      dispatcher ! msg
    //      subscriptions = subscriptions + x
    case msg: XUnSubscribe =>
      dispatcher ! msg
    //subscriptions = subscriptions - x
  }

  def broadcast(msg: Any): Unit = {
    dispatcher ! XBroadcast(msg)
  }


}
