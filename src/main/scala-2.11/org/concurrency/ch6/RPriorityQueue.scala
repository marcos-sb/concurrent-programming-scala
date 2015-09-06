package org.concurrency.ch6

import rx.lang.scala.{Subject, Observable}
import scala.collection.mutable.PriorityQueue

class RPriorityQueue[T](implicit ord: Ordering[T]) {
  private val pq = PriorityQueue[T]()(ord)
  private val su = Subject[T]()

  def add(x: T): Unit = pq.enqueue(x)
  def pop(): T = {
    val top = pq.dequeue()
    su.onNext(top)
    top
  }
  def popped: Observable[T] = su
}

object RPriorityQueueApp extends App {
  //max pq
  val rpq = new RPriorityQueue[Int]()(Ordering.by((x:Int) => x))
  rpq.add(1)
  rpq.add(2)
  rpq.add(3)

  rpq.pop() //pop 3
  //1,2 in the queue
  //no prints to console

  rpq.popped.subscribe(println(_))
  rpq.pop()
  rpq.pop()
  //2\n1 printed to console
}
