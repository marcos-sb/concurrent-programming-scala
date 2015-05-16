package org.concurrency.ch2

object Thread {
  def thread(body: => Unit) = {
    val t = new Thread {
      override def run() = body
    }
    t.start()
    t
  }
}
