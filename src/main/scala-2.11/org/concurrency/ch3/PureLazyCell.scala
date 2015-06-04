package org.concurrent.ch3

import java.util.concurrent.atomic
import scala.annotation.tailrec

class PureLazyCell[T](initialization: => T) {
  private val evaluated = new atomic.AtomicBoolean()
  private var _val:T = _
  @tailrec
  final def apply(): T = {
    val _evaluated = evaluated.get
    if(_evaluated) _val
    else {
      if(!evaluated.compareAndSet(_evaluated, true)) apply()
      else {
        _val = initialization
        _val
      }
    }
  }
}

object PureLazyCellApp extends App {
  val _lazy = new PureLazyCell[Int]({5/3})
  println(_lazy())
  _lazy()
  val _lazy2 = new PureLazyCell[Int]({3/3})
//  lazy val _lazy3 = println(3/4)
//  _lazy3; _lazy3
}
