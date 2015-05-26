package org.concurrency.ch2

import scala.annotation.tailrec
import scala.collection.mutable

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
          state = None // if state.notify() was called afterwards, an IllegalMonitorStateExc would be thrown as the current thread does not own the object monitor anymore; it was lost on assigning None to state
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

//  val sv2 = new SyncVar2[Int]
//  val tprod = Thread.thread {
//    sv2.synchronized {
//      for (i <- 0 until 15) {
//        while (sv2.nonEmpty) sv2.wait()
//        sv2.put(i)
//        sv2.notify()
//      }
//    }
//  }
//  val tcons = Thread.thread {
//    @tailrec
//    def go(): Unit = {
//      var get: Int = 0
//      sv2.synchronized {
//        while (sv2.isEmpty) sv2.wait()
//        get = sv2.get()
//        sv2.notify()
//      }
//      println(get)
//      if (get < 14) go()
//    }
//    go()
//  }

  // 5
  // the exs before are not the best coding as they synchronize at the state var and at the SyncVar
  class SyncVar3[T] {
    private val lock = new AnyRef
    var state: Option[T] = None
    final def getWait(): T = lock.synchronized {
      state match {
        case Some(x) =>
          val tmp: T = x
          state = None
          lock.notify()
          tmp
        case None => lock.wait(); getWait()
      }
    }
    final def putWait(x: T): Unit = lock.synchronized {
      state match {
        case None => state = Some(x); lock.notify()
        case Some(v) => lock.wait(); putWait(v)
      }
    }
  }

  //  val sv2 = new SyncVar3[Int]
  //  val tprod = Thread.thread {
  //      for (i <- 0 until 15) {
  //        sv2.putWait(i)
  //      }
  //    }
  //
  //  val tcons = Thread.thread {
  //    @tailrec
  //    def go(): Unit = {
  //      val get: Int = sv2.getWait()
  //      println(get)
  //      if (get < 14) go()
  //    }
  //    go()
  //  }

  // 6
  class SyncQueue[T](val n:Int) {
    val q: mutable.Queue[T] = new mutable.Queue()

    final def getWait(): T = q.synchronized {
      while(q.isEmpty) q.wait()
      val v = q.dequeue()
      q.notify()
      v
    }

    final def putWait(el: T) = q.synchronized {
      while(n < q.size) q.wait()
      q += el
      q.notify()
    }
  }

//  val sq = new SyncQueue[Int](3)
//  val t1 = Thread.thread {
//    for(i <- 0 until 15) sq.putWait(i)
//  }
//  val t2 = Thread.thread {
//    var v:Int = sq.getWait()
//    while(v < 14) {
//      println(v); v = sq.getWait()
//    }
//  }

  // 7
  class Account(val name:String, var bal:Int) {
    override def equals(other: Any):Boolean = other match {
      case that:Account => this.name == that.name
      case _ => false
    }
    override def hashCode = name.hashCode
  }
  private def send(src: Account, dst: Account, quantity:Int): Unit = {
    Thread.thread {
      src.synchronized {
        dst.synchronized {
          src.bal -= quantity
          dst.bal += quantity
        }
      }
    }
  }
  // this code supposes target is not in accounts
  def sendAll(accounts:collection.immutable.Set[Account], target:Account): Unit = {
    if(!accounts.contains(target))
      for(acc <- accounts) send(acc, target, acc.bal)
  }

//  val dst = new Account("target", 0)
//  sendAll((for(i <- 10 until 20) yield new Account("src", i)).toSet, dst)
//  println(dst.bal)
}
