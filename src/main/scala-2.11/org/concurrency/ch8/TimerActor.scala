package org.concurrency.ch8

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._


object TimerActor {

  case class Register(ar: ActorRef, timeout: Duration = 1.second)

  case object Timeout

}

class TimerActor extends Actor {
  private val log = Logging(context.system, this)

  def receive = {
    case TimerActor.Register(ar, timeout) =>
      Future {
        Thread.sleep(timeout.toMillis)
        ar ! TimerActor.Timeout
      }

    case TimerActor.Timeout =>
      log.info("timeout")
  }

  override def unhandled(msg: Any) =
    log.info(s"cannot handle that something you sent in")
}

object TimerApp extends App {

  lazy val actorSystem = ActorSystem("TestSystem")
  val ta1 = actorSystem.actorOf(Props[TimerActor], name = "timer1")
  val ta2 = actorSystem.actorOf(Props[TimerActor], name = "timer2")

  ta1 ! TimerActor.Register(ta2)
  ta1 ! TimerActor.Register(ta2)
  ta1 ! TimerActor.Register(ta2)

  ta1 ! "hey, I'm just testing that I shall be consumed with no delay..."

  println("I'm done, but timeouts are pending...")
}
