package org.concurrency.ch2

import scala.collection._

object SynchronizedPool extends App {
  private val tasks = mutable.Queue[() => Unit]()
  object Worker extends java.lang.Thread {
    setDaemon(true)
    def poll() = tasks.synchronized {
      while(tasks.isEmpty) tasks.wait()
      tasks.dequeue()
    }
    override def run() = while(true) {
      val task = poll()
      task()
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
}
