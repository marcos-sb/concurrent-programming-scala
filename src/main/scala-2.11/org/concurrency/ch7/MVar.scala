package org.concurrency.ch7

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.stm._

class MVar[T] {
  private val v = Ref[T](null.asInstanceOf[T])

  def put(x:T)(implicit txn: InTxn): Unit = {
    if(v() == null) v() = x
    else retry
  }

  def take()(implicit txn: InTxn): T = {
    if(v() != null) {
      val tmp = v()
      v() = null.asInstanceOf[T]
      tmp
    } else retry
  }
}

object MVar {
  def swap[T](a: MVar[T], b: MVar[T])(implicit txn: InTxn): Unit = {
    val tmp = a.v()
    a.v() = b.v()
    b.v() = tmp
  }
}

object MVarApp extends App {

  //it should print as\nasas

  val mv = new MVar[String]
  val mv2 = new MVar[String]
  Future {
    atomic {
      implicit tx => {
        mv.put("as")
      }
    }
    atomic {
      implicit tx => {
        mv.put("asas")
      }
    }
    atomic {
      implicit tx => {
        MVar.swap(mv, mv2)
      }
    }
    atomic {
      implicit tx => {
        println(mv2.take) //print asas
      }
    }
  }

  Future {
    atomic {
      implicit tx => {
        println(mv.take)
      }
    }
  }

  Thread.sleep(2000)
}