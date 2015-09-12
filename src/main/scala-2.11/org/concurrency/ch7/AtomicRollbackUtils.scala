package org.concurrency.ch7

import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.stm._
import scala.concurrent.{Future, blocking}

object AtomicRollbackUtils {
  def rollbackCount[T](block: InTxn => T): (T, Int) = {
    val count = new AtomicInteger(-1)
    atomic {
      implicit tx => {
        count.incrementAndGet()
        (block(tx), count.get())
      }
    }
  }

  def rollBackWithRetryMax[T](n: Int)(block: InTxn => T): T = {
    val count = new AtomicInteger(-1)
    atomic {
      implicit tx => {
        if (count.incrementAndGet() < n) block(tx)
        else sys.error("Max. retries reached for Tx.")
      }
    }
  }
}

object AtomicRollbackUtilsApp extends App {

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
      println(s"${AtomicRollbackUtils.rollbackCount(block)}")
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
