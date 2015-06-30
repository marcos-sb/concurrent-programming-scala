package org.concurrency.ch5

import scala.util.Random
import java.util.concurrent.atomic.AtomicInteger

object WhitespaceRandomizer {
  def count(s: String, c: Char): Int = {
    var cc = 0
    s foreach {
      case x if c == x => cc += 1
      case _ =>
    }
    cc
  }

  def count(ps: ParString, c: Char): Int = {
    val uid = new AtomicInteger(0)
    ps foreach {
      case x if x == c => uid.incrementAndGet()
      case _ =>
    }
    uid.intValue()
  }
}

class WhitespaceRandomizer {
  private val rnd = new Random()
  def generate(p: Double, sz: Int): String = {
    val sb:StringBuilder = new StringBuilder()
    for (_ <- 0 until sz) {
      sb += (if (rnd.nextDouble() < p) ' '
      else rnd.nextPrintableChar())
    }
    sb.toString()
  }
}


object WhitespaceRandomizerApp extends App {
  val wsrnd = new WhitespaceRandomizer()
  val sz = 10000000

  for (p <- 0.0d to 1.0d by 0.1d) {
    val s = wsrnd.generate(p, sz)
    print(s"(p:$p, c:${WhitespaceRandomizer.count(s, ' ')})")
    println(s"(p:$p, t:${Timer.warmedTimed(50)(WhitespaceRandomizer.count(s, ' '))})")
  }

  for (p <- 0.0d to 1.0d by 0.1d) {
    val ps = new ParString(wsrnd.generate(p, sz))
    print(s"(p:$p, c:${WhitespaceRandomizer.count(ps, ' ')})")
    println(s"(p:$p, t:${Timer.warmedTimed(50)(WhitespaceRandomizer.count(ps, ' '))})")
  }
}
