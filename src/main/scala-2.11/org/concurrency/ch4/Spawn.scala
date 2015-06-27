package org.concurrency.ch4

import scala.sys.process._
import scala.async.Async.async
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object Spawn {
  def spawn(command: String): Future[Int] = {
    async {
      command.!
    }
  }
}

object SpawnApp extends App {
  Spawn.spawn("date") foreach {
    case x => println(s"exitcode: $x")
  }

  for {
    x <- Spawn.spawn("date")
  } yield println(s"exitcode: $x")

  for (
    x <- Spawn.spawn("date")
  ) { println(s"exitcode: $x") }

  Thread.sleep(1000)
}
