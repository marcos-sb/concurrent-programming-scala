package org.concurrency.ch3

import scala.concurrent.ExecutionContext

class PiggybackContext extends ExecutionContext {

  override def execute(runnable: Runnable):Unit = {
    runnable.run()
  }

  override def reportFailure(cause: Throwable): Unit = {
    throw cause
  }
}

object Ex1 extends App {
  val pc = new PiggybackContext()

  val r = new Runnable {
    override def run(): Unit = {
      println(Thread.currentThread.getName)
      pc.execute(new Runnable {override def run() = println("nested run")})
      println(1 / 0)
    }
  }

  try {
    pc.execute(r)
  } catch {
    case ex:Exception => println(ex.getMessage)
  }
}
