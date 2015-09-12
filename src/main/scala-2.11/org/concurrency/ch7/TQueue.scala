package org.concurrency.ch7

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, blocking}
import scala.concurrent.stm._

class TQueue[T] {

  private class Node(val value: T) {
    var next: Node = _
  }

  private val head = Ref[Node](null)
  private val tail = Ref[Node](null)

  def enqueue(x: T)(implicit tx: InTxn): Unit = {
    if (tail() == null) {
      tail() = new Node(x)
      head() = tail()
    }
    else {
      tail().next = new Node(x)
      tail() = tail().next
    }
  }

  def dequeue()(implicit tx: InTxn): T = {
    if (head() == null) retry
    else {
      val res = head().value
      head() = head().next
      if (head() == null) tail() == null //0 elements in the queue
      res
    }
  }
}

object TQueueApp extends App {
  val q = new TQueue[Int]()

  Future {
    atomic {
      implicit tx => {
        q.enqueue(1)
      }
    }
    println("wait and insert next")
    Thread.sleep(1000)

    atomic {
      implicit tx => {
        q.enqueue(2)
      }
    }
  }

  Future {
    blocking {
      atomic {
        implicit tx => {
          println(q.dequeue())
          println(q.dequeue())
        }
      }
    }
  }

  Thread.sleep(3000)
}