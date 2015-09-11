package org.concurrency.ch7

import scala.concurrent.stm._

class TPair[P,Q](pinit:P, qinit:Q) {
  val p = Ref[P](pinit)
  val q = Ref[Q](qinit)

  def first(implicit txn: InTxn): P = p()
  def first_= (x:P)(implicit txn: InTxn): P = { p() = x; p() }

  def second(implicit txn: InTxn): Q = q()
  def second_=(x: Q)(implicit txn: InTxn): Q = { q() = x; q() }

  def swap()(implicit e: P =:= Q, txn:InTxn):Unit = {
    val r = q().asInstanceOf[P]
    q() = p().asInstanceOf[Q]
    p() = r
  }

  override def toString: String = {
    atomic {
      implicit tx => {
        s"(${p()},${q()})"
      }
    }

  }
}

object TPairApp extends App {
  atomic {
    implicit tx => {
      val p = new TPair("1", 1)
      println(p)

      p.first = "2"
      p.second = 2
      println(p)

      //compile error
//      println({
//        p.swap
//        p
//      })
    }
  }
  atomic {
    implicit tx => {
      val q = new TPair(1, 2)
      println({
        q.swap; q
      })
    }
  }
}
