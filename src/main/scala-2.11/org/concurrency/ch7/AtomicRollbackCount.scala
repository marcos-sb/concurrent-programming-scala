package org.concurrency.ch7

import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, blocking}
import scala.concurrent.stm._

object AtomicRollbackCount {
  def count[T](block: InTxn => T): (T, Int) = {
    val count = new AtomicInteger(-1)
    atomic {
      implicit tx => {
        count.incrementAndGet()
        (block(tx), count.get())
      }
    }
  }
}

object AtomicRollbackCountApp extends App {

  val mv1 = new MVar[Boolean]()
  val mv2 = new MVar[Boolean]()
  val block = (tx: InTxn) => atomic {
    implicit tx => {
      if (mv1.take && mv2.take) "end"
      else retry
    }
  }

  Future {
    blocking {
      println(s"${AtomicRollbackCount.count(block)}")
    }
  }

  Thread.sleep(1000)

  Future {
    atomic {
      implicit tx => {
        mv1.put(true)
      }
    }

    Thread.sleep(1000)

    atomic {
      implicit tx => {
        mv2.put(true)
      }
    }
  }

  Thread.sleep(2000)
}
