package org.concurrency.ch3

import java.util.concurrent.atomic
import scala.annotation.tailrec

class TreiberStack[T] {
  class Node(val v:T, var next:Node)

  private val head = new atomic.AtomicReference[Node]

  @tailrec
  final def push(x:T): Unit = {
    val oldhead = this.head.get
    val newhead = new Node(x, oldhead)
    if(!head.compareAndSet(oldhead, newhead)) push(x)
  }
  @tailrec
  final def pop(): Option[T] = {
    val oldhead = this.head.get
    if(oldhead != null) {
      val newhead = oldhead.next
      if (head.compareAndSet(oldhead, newhead)) Some(oldhead.v)
      else pop()

    } else None
  }
}

object Ex2 extends App {
  val ts = new TreiberStack[Int]

  for(i <- 0 until 10) {
    concurrent.ExecutionContext.global.execute(
      new Runnable {
        override def run() = {
          ts.push(i)
          Thread.sleep(1000)
          println(ts.pop())
        }
      }
    )
  }
  Thread.sleep(12000)
}
