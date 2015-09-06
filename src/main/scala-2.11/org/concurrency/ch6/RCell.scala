package org.concurrency.ch6

import rx.lang.scala.Subject

class RCell[T] extends {val su = Subject[T]()} with Signal[T](su) {
  def :=(x: T):Unit = {
    su.onNext(x)
  }
}

object RCellApp extends App {
  val cell = new RCell[String]()
  cell := "hi"
  println(cell())
}
