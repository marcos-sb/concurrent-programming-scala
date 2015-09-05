package org.concurrency.ch6

import rx.lang.scala.Observable

class Signal[T](val ob: Observable[T]) {
  var v: T = _
  ob.subscribe(v => this.v = v)

  def apply(): T = v
  def map[S](f: T => S): Signal[S] = new Signal(ob.map(f))
  def zip[S](that: Signal[S]): Signal[(T,S)] = new Signal(ob.zip(that.ob))
  def scan[S](z:S)(f: (S,T) => S) = new Signal(ob.scan(z)(f))
}


object SignalApp extends App {
  implicit final class SignalExtensions[T](val source: Observable[T]) {
    def toSignal:Signal[T] = new Signal(source)
  }

  val s = Observable.from(List("foo", "bar")).toSignal
  println(s())

  val s1 = Observable.from(List("0", "1", "2", "3")).toSignal.map(_.toInt)
  println(s1())

  val s2 = Observable.from(List("zero", "one", "two")).toSignal
  val s3 = Observable.from(List("0", "1", "2")).toSignal
  println(s2.zip(s3)())

  val s4 = s1.scan(0)(_+_)
  println(s4())
}
