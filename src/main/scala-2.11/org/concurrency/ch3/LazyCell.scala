package org.concurrency.ch3

class LazyCell[T](initialization: => T) {
  @volatile private var _eval = false
  private var _value: T = _
  def apply(): T =
    if(_eval) _value
    else this.synchronized {
      _eval = true; _value = initialization
      _value
    }
}

object LazyCellRun extends App {
  val _lazy = new LazyCell({println(1)})
  _lazy()
  _lazy()
  val _lazy2 = new LazyCell({println(2)})
  lazy val _lazy3 = println(3)
  _lazy3; _lazy3
}
