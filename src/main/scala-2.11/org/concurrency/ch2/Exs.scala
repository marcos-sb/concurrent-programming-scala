package org.concurrency.ch2

import scala.annotation.tailrec

object Exs extends App {
  // 1
  def parallel[A,B] (a: => A, b: => B): (A,B) = {
    var resA:A = null.asInstanceOf[A]
    var resB:B = null.asInstanceOf[B]
    val tA = Thread.thread {
      resA = a
    }
    val tB = Thread.thread {
      resB = b
    }
    tA.join(); tB.join()
    (resA, resB)
  }
//  println(parallel(1+2,3+3))

  // 2
  def periodically(duration: Long, count: Int) (b: => Unit): Unit = {
    var i = 0
    var t = null.asInstanceOf[java.lang.Thread]
    while(i < count) {
      i+=1
      t = Thread.thread {b}

      java.lang.Thread.sleep(duration)
    }
  }

  //periodically(2000, 2) {println(3+4)}

  // 3
  class SyncVar1[T] {
    private var state: Option[T] = None
    def get(): T = state.synchronized { // if synchronized were invoked on a var state initialized as '= _' it would throw a NullPointerExc
      state match {
        case None => throw new IllegalArgumentException()
        case Some(x) =>
          val tmp: T = x
          state = None
          tmp

      }
    }
    def put(el: T): Unit = state.synchronized {
      state match {
        case None => state = Some(el)
        case Some(x) => throw new IllegalArgumentException()
      }
    }
  }

//  val sv = new SyncVar1[Int]
//  val t1 = Thread.thread {
//    sv.put(5)
//  }
//  val t2 = Thread.thread {
//    println(sv.get())
//  }
//
//  t1.join(); t2.join()


  // 4
  class SyncVar2[T] {
    private var state: Option[T] = None
    def get(): T = state.synchronized {
      state match {
        case None => throw new IllegalArgumentException()
        case Some(x) => 
          val tmp: T = x
          state = None
          tmp
      }
    }
    def put(el: T): Unit = state.synchronized {
      state match {
        case None => state = Some(el)
        case Some(x) => throw new IllegalArgumentException()
      }
    }
    def isEmpty: Boolean = state.synchronized {
      state match {
        case None => true
        case _ => false
      }
    }
    def nonEmpty: Boolean = !isEmpty
  }

  val sv2 = new SyncVar2[Int]
  val tprod = Thread.thread {
    sv2.synchronized {
      for (i <- 0 until 15) {
        while (sv2.nonEmpty) sv2.wait()
        sv2.put(i)
        sv2.notify()
      }
    }
  }
  val tcons = Thread.thread {
    @tailrec
    def go(): Unit = {
      var get: Int = 0
      sv2.synchronized {
        while (sv2.isEmpty) sv2.wait()
        get = sv2.get()
        sv2.notify()
      }
      println(get)
      if (get < 14) go()
    }
    go()
  }
}
