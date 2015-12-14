package org.concurrency.ch8

import akka.actor._
import akka.event.Logging
import akka.pattern.{AskTimeoutException, ask}
import akka.util.Timeout
import rx.lang.scala.Observable

import scala.concurrent.duration._

object FailureDetectorActor {
  case class Identify(ref:ActorRef, interval:Duration)
  case object Identify
  case class ActorIdentity(ref:ActorRef)
  case class Failed(ref:ActorRef)
  case object Stop

  def props(threshold: FiniteDuration): Props =
    Props(new FailureDetectorActor(threshold))
}

class FailureDetectorActor(val threshold:FiniteDuration) extends Actor {
  import FailureDetectorActor._

  import scala.concurrent.ExecutionContext.Implicits.global

  private val thres:Timeout = Timeout(threshold)
  private val logger = Logging(context.system,this)

  override def receive = {
    case Identify(ref, interval) =>
      val o = Observable.interval(interval).subscribe {
        _ =>
          val ack = ref.ask(Identify)(thres)
          val parent = context.parent
          ack onFailure {
            case ex: AskTimeoutException =>
              logger.error(ex, s"Heartbeat not received from $ref")
              parent ! FailureDetectorActor.Failed(ref)
          }
          ack onSuccess {
            case ActorIdentity(acRef) =>
              logger.info(s"heartbeat from $acRef")
          }
      }
  }

}

class TrackedActor extends Actor {
  private val logger = Logging(context.system,this)
  override def receive = {
    case FailureDetectorActor.Identify =>
      /* Toggle comment of the following line to change behavior
      *  from throwing AskTimeoutException to printing received heartbeat
      */
//      sender ! FailureDetectorActor.ActorIdentity(self)

    case FailureDetectorActor.Stop =>
      context.stop(self)
  }
}

object FailureDectectorApp extends App {
  val actorSystem = ActorSystem("FDA")
  val a1 = actorSystem.actorOf(FailureDetectorActor.props(2.seconds),name="a1")
  val a2 = actorSystem.actorOf(Props[TrackedActor], name="a2")

  a1 ! FailureDetectorActor.Identify(a2,3.second)

  Thread.sleep(10000)
  /* time-to-live of the simulation of 10s with a
  *  heartbeat delay of 3s and a
  *  threshold of 2s equals three AskTimeoutExceptions
  */
  actorSystem.terminate()
}