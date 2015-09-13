package org.concurrency.ch7

import scala.collection.mutable
import scala.concurrent.stm._
import scala.reflect.ClassTag

class TArrayBuffer[T: ClassTag] extends mutable.Buffer[T] {

  private val arr = TArray.ofDim[T](16)
  private val top = Ref[Int](0)

  override def apply(n: Int): T = arr.single(n)

  override def update(n: Int, newelem: T): Unit = arr.single.update(n, newelem)

  override def clear(): Unit = atomic {
    implicit tx =>
      for (i <- 0 until top()) arr(i) = null.asInstanceOf[T]
  }

  override def length: Int = top.single()

  override def remove(n: Int): T = atomic {
    implicit tx =>
      if (top() <= n) throw new ArrayIndexOutOfBoundsException(0)
      val removed = arr(n)
      for (i <- n until top() - 1) arr(i) = arr(i + 1)
      top() = top() - 1
      removed
  }

  override def +=:(elem: T): this.type = atomic {
    implicit tx =>
      for (i <- 1 to top()) arr(i) = arr(i - 1)
      arr(0) = elem
      top() = top() + 1
      this
  }

  override def +=(elem: T): this.type = atomic {
    implicit tx =>
      arr(top()) = elem
      top() = top() + 1
      this
  }

  override def insertAll(n: Int, elems: Traversable[T]): Unit = atomic {
    implicit tx =>
      var i = n
      for (el <- elems) {
        arr(i) = el; i += 1
      }
  }

  override def iterator: Iterator[T] = atomic {
    implicit tx =>

      new Iterator[T] {
        private val currRef = Ref(0)

        override def hasNext: Boolean = atomic {
          implicit tx =>
            currRef() < top()
        }

        override def next(): T = atomic {
          implicit tx =>
            val el = arr(currRef())
            currRef() = currRef() + 1
            el
        }
      }
  }
}

object TArrayBufferApp extends App {
  val tarr = new TArrayBuffer[Int]

  tarr += 1
  tarr += 2

  println(tarr)

  try {
    atomic {
      implicit tx =>
        println(tarr remove 0)
        println(tarr remove 0)
        tarr remove 0
    }
  } catch {
    case e: Exception => println(e)
  }

  println(tarr)
}