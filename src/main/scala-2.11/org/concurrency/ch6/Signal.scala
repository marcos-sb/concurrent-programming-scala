package org.concurrency.ch6

import rx.lang.scala.{Subscription, Observable}
import rx.lang.scala.subjects.BehaviorSubject

trait Signal[T] {
  abstract def bs: BehaviorSubject[T]
  abstract def subs: Subscription

  def apply(): T = {
    bs.getValue
  }
  def map[S](f: T => S): Signal[S] = {
    val oldbs = bs
    new Signal[S] {
      def bs = BehaviorSubject[S]()
      def subs = oldbs.map(f).subscribe(bs)
    }
  }
  def zip[S](that: Signal[S]): Signal[(T,S)] = {
    val oldbs = bs
    new Signal[(T,S)] {
      def bs = BehaviorSubject[(T,S)]()
      def subs = oldbs.zip(that.bs).subscribe(bs)
    }
  }
  def scan[S](z: S)(f:(S,T) => S): Signal[S] = {
    val oldbs = bs
    new Signal[S] {
      def bs = BehaviorSubject[S]()
      def subs = oldbs.scan(z)(f).subscribe(bs)
    }
  }
}

object SignalApp extends App {
  implicit final class SignalExtensions[T](val source: Observable[T]) {
    def toSignal:Signal[T] = new Signal[T] {
      def bs = BehaviorSubject[T]()
      def subs = source.subscribe(bs)
    }
  }
}
