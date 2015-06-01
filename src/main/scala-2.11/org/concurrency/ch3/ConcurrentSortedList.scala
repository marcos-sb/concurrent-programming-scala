package org.concurrency.ch3

import java.util.concurrent.atomic
import scala.annotation.tailrec

class ConcurrentSortedList[T](implicit val ord: Ordering[T]) extends Iterable[T] {

  final class Node(val v: T, var next: atomic.AtomicReference[Node]) {
    def this(v: T) {
      this(v, new atomic.AtomicReference)
    }
    override def toString = s"$v -> $next" // note: linear time vs. expected constant
  }

  private val _head = new atomic.AtomicReference[Node]

  def add(x: T): Unit = {
    val current = new atomic.AtomicReference(_head.get)
    while(current.get != null && ord.gt(x, current.get.v) &&
      current.get.next.get != null && ord.gt(x, current.get.next.get.v)) {
      if (!current.compareAndSet(current.get, current.get.next.get))
        add(x)
    }
    if(current.get == null || !ord.gt(x, current.get.v)) {
      if (!_head.compareAndSet(_head.get, new Node(x, current))) add(x)
    } else {
      val currNext = new atomic.AtomicReference(current.get.next.get)
      if (!current.get.next.compareAndSet(current.get.next.get,
        new Node(x, currNext))) add(x)
    }
  }

  def iterator: Iterator[T] = new Iterator[T] {
    val current = new atomic.AtomicReference(_head.get)

    override def hasNext: Boolean = current.get != null

    @tailrec
    override def next(): T = {
      val ret = current.get.v
      if(!current.compareAndSet(current.get, current.get.next.get)) next()
      else ret
    }
  }

}

object Ex3 extends App {
  val l = new ConcurrentSortedList[Int]
  for(i <- 0 until 3) {
    concurrent.ExecutionContext.global.execute(
      new Runnable {
        override def run(): Unit = {
          l.add(1); l.add(4); l.add(2); l.add(3); l.add(3)
          println(l.mkString(" "))
        }
      }
    )
  }
  Thread.sleep(5000)
  l.add(1); l.add(4); l.add(2); l.add(3); l.add(3)
  println("------------------")
  println(l.mkString(" "))
}
