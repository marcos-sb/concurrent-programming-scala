package org.concurrency.ch2

object Thread {
  def thread(body: => Unit):java.lang.Thread = {
    val t = new Thread {
      override def run() = body
    }
    t.start()
    t
  }
}
