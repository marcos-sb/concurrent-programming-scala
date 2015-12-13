
package org.concurrency.ch8

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging

import scala.concurrent.ExecutionContext

class ExecutionContextActor(val parallelism:Int = 2) extends ExecutionContext {

  lazy val actorSystem = ActorSystem("ExContextActorSystem")
  val router = actorSystem.actorOf(Props(new RouterActor(parallelism)), name="router-actor")

  override def execute(runnable: Runnable) =
    router ! ExecutionActor.Execute(runnable)

  override def reportFailure(cause: Throwable) =
    println("something blew up...", cause)
}

private object ExecutionActor {
  case class Execute(runnable: Runnable)
}

private class RouterActor(val actorCount:Int = 2) extends Actor {
  var nextActor = 0
  val delegates = for(n <- 0 until actorCount)
    yield context.actorOf(Props[ExecutionActor], name=s"exec-actor-$n")

  override def receive = {
    case msg @ ExecutionActor.Execute(_) =>
      delegates(nextActor) ! msg
      nextActor = (nextActor + 1) % actorCount
  }
}

private class ExecutionActor extends Actor {
  val logger = Logging(context.system, this)
  override def receive = {
    case ExecutionActor.Execute(runnable) =>
      runnable.run()
  }
}

object ExContextApp extends App {
  implicit val exeContext:ExecutionContext = new ExecutionContextActor()
//  implicit val exeContext = ExecutionContext.Implicits.global

  exeContext.execute(new Runnable {
    override def run() = {
      println(5 / 0)
    }
  })

  Thread.sleep(3000)
}