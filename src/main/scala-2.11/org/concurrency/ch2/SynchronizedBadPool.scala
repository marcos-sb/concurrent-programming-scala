package org.concurrency.ch2

import scala.collection._

object SynchronizedBadPool extends App {
  private val tasks = mutable.Queue[() => Unit]()

  val worker = new java.lang.Thread {
    def poll(): Option[() => Unit] = tasks.synchronized {
      if(tasks.nonEmpty) Some(tasks.dequeue())
      else None
    }
    override def run() = while(true) poll() match {
      case Some(task) => task()
      case None =>
    }
  }

  worker.setName("Worker")
  worker.setDaemon(true)
  worker.start()

  def asynchronous(body: => Unit) = tasks.synchronized {
    tasks.enqueue(() => body)
  }
  asynchronous {println("Hello")}
  asynchronous {println("World")}
  java.lang.Thread.sleep(5000)
}
