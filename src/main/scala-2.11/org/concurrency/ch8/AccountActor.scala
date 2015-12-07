package org.concurrency.ch8

import akka.actor.{Actor, ActorRef, ActorSystem, Kill, Props}
import akka.event.Logging

object AccountActor {

  case class Send(amount: Int, account: ActorRef)

  case class Rcv(amount: Int)

  case object Amount

}

class AccountActor(
                    private var amount: Int
                  ) extends Actor {

  private val log = Logging(context.system, this)

  def receive = {
    case AccountActor.Send(am, aa) =>
      log.info(s"received SEND($am -> $aa)")
      amount -= am
      aa ! AccountActor.Rcv(am)

    case AccountActor.Rcv(am) =>
      log.info(s"received RCV($am)")
      amount += am

    case AccountActor.Amount =>
      log.info(amount.toString)
  }

  override def unhandled(msg: Any) = {
    log.info("I cannot understand that...")
  }

}

object AccountsApp extends App {
  lazy val actorSystem = ActorSystem("Bank")

  val ac1 = actorSystem.actorOf(Props(new AccountActor(100)), name = "ac1")
  val ac2 = actorSystem.actorOf(Props(new AccountActor(100)), name = "ac2")

  ac1 ! AccountActor.Send(10, ac2)

  ac1 ! AccountActor.Amount
  ac2 ! AccountActor.Amount

  ac1 ! Kill

  Thread.sleep(5000)
  actorSystem.terminate
}

/**
  * Kill may be considered a regular message with really high priority as it becomes the head of the mailbox
  * of any Actor as soon as it is received, with no preemption of the currently executing message handler. The Kill
  * message causes the recipient Actor to raise an ActorKilledException, which by default restarts the Actor
  * with no message loss on the mailbox.
  * With this in mind, it is easy to see why receiving a Kill message at any time during execution won't impact the
  * correctness of the transfer between accounts.
  */
