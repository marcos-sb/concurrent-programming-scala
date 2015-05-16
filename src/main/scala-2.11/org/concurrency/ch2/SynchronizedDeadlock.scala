package org.concurrency.ch2

import SynchronizedNesting.Account

object SynchronizedDeadlock extends App {
  def send(a: Account, b: Account, n: Int) = a.synchronized {
    b.synchronized {
      a.money -= n
      b.money += n
    }
  }

  val a = new Account("Jack", 1000)
  val b = new Account("Jill", 2000)
  val t1 = Thread.thread {
    for(i <- 0 until 100) send(a,b,1)
  }
  val t2 = Thread.thread {
    for(i <- 0 until 99) send(b,a,1)
  }
  t1.join(); t2.join()
  println(s"a = ${a.money}, b = ${b.money}")
}
