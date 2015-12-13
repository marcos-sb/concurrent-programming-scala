package org.concurrency.ch8

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging

object SessionActor {
  case class StartSession(pass: String)
  case object EndSession

  def props(password:String, ref:ActorRef):Props =
    Props(new SessionActor(password,ref))
}

class SessionActor
  (private val password: String,
    private val r: ActorRef)
  extends Actor {

  private val log = Logging(context.system, this)

  override def receive = prestart

  private def prestart: Actor.Receive = {
    case SessionActor.StartSession(`password`) =>
      log.info("password ok!, starting session...")
      context.become(started)

    case SessionActor.StartSession(wrongPass) =>
      log.info(s"wrong pass $wrongPass")
  }

  private def started: Actor.Receive = {
    case SessionActor.EndSession =>
      log.info("finishing session...")
      context.stop(self)

    case msg =>
      r forward msg

  }
}

object SessionApp extends App {
  val actorSystem = ActorSystem("SessionActorSystem")

  val ac1 = actorSystem.actorOf(AccountActor.props(100), name="account1")
  val ac2 = actorSystem.actorOf(AccountActor.props(100), name="account2")
  val secAc = actorSystem.actorOf(SessionActor.props("1234", ac2),
    name="secured_account2")

  //no go
  secAc ! SessionActor.StartSession("'wrongpass'")

  //no session
  secAc ! AccountActor.Send(15, ac1)

  //session start
  secAc ! SessionActor.StartSession("1234")

  //no AccountActor msg
  secAc ! "unhandle me"

  //transfer money
  secAc ! AccountActor.Send(15, ac1)

  //check accounts balance
  secAc ! AccountActor.Amount

  //the next message gets reordered...
  ac1 ! AccountActor.Amount

  //so let's give the actors a break
  Thread.sleep(1000)
  ac1 ! AccountActor.Amount

  //finish the session
  secAc ! SessionActor.EndSession

  //dead letter...
  secAc ! AccountActor.Amount

  Thread.sleep(4000)
  actorSystem.terminate
}
