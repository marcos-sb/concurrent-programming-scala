package org.concurrency.ch2

object Exs extends App {
  // 1
  def parallel[A,B] (a: => A, b: => B): (A,B) = {
    var resA:A = null.asInstanceOf[A]
    var resB:B = null.asInstanceOf[B]
    val tA = Thread.thread {
      resA = a
    }
    val tB = Thread.thread {
      resB = b
    }
    tA.join(); tB.join()
    (resA, resB)
  }
  println(parallel(1+2,3+3))
}
