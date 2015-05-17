package org.concurrency.ch2

import scala.collection._
import scala.annotation.tailrec

object SynchronizedGracefulPool extends App {
  private val tasks = mutable.Queue[() => Unit]()
  object Worker extends java.lang.Thread {
    var terminated = false
    def poll():Option[() => Unit] = tasks.synchronized {
      while(tasks.isEmpty && !terminated) tasks.wait()
      if(!terminated) Some(tasks.dequeue())
      else None
    }
    @tailrec
    override def run() = poll() match {
      case Some(task) => task(); run()
      case None =>
    }
    def shutdown() = tasks.synchronized {
      terminated = true
      tasks.notify()
    }
  }
  Worker.start()
  def asynchronous(body: => Unit) = tasks.synchronized {
    tasks.enqueue(() => body)
    tasks.notify()
  }
  asynchronous {println("Hello ")}
  asynchronous {println("World!")}
  java.lang.Thread.sleep(500)
  Worker.shutdown()
}
