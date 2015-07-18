package org.concurrency.ch5

object Timer {
  @volatile var dummy: Any = _

  def timed[T](body: => T, times:Int = 1): Double = {
    val start = System.nanoTime
    for(_ <- 0 until times) dummy = body
    val end = System.nanoTime
    (((end - start) / 1000) / 1000.0) / times
  }

  def warmedTimed[T](n: Int = 200, times: Int = 1)(body: => T):Double = {
    for(_ <- 0 until n) body
    timed(body, times)
  }
}
