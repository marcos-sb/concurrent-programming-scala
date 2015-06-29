package org.concurrency.ch5

object MeasureAllocApp extends App {
  println(s"new String(): ${Timer.warmedTimed()(new String())} ms")
}
