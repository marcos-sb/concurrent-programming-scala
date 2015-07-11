package org.concurrency.ch5

import scala.annotation.tailrec
import scala.collection.GenSeq
import scala.util.Random


class CellAutomaton (var state:GenSeq[Boolean]) {

  def evolve(par: Boolean): Unit = {
    val range = 1 until state.length - 1
    val next = new Array[Boolean](state.length)
    for (i <- if(par) range.par else range) {
      next(i) = (state(i-1), state(i), state(i + 1)) match {
        case (true, v, true) => !v
        case (true, v, _) => v
        case (_, v, true) => v
        case _ => false
      }
    }
    next(0) = if (state(1)) state.head else false
    next(state.length - 1) = if (state(state.length-2)) state.last else false
    state = next
  }

  def evolve(iters: Int = 10, par: Boolean = false): Unit = {
    @tailrec
    def go(n: Int): Unit = {
//      println(state.map(if(_) 1 else 0).mkString)
      if(n > 0) {
        evolve(par)
        go(n-1)
      }
    }
    go(iters)
  }
}

object CellAutomatonApp extends App {
  val cell = new CellAutomaton(Vector.tabulate(20000)(_ => Random.nextBoolean()))
  val timeSeq = Timer.warmedTimed()(cell.evolve(10, false))
  val timePar = Timer.warmedTimed()(cell.evolve(10, true))
  println(s"tseq: $timeSeq ms")
  println(s"tpar: $timePar ms")
}
