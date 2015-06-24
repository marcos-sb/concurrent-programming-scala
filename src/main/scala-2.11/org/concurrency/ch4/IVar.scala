package org.concurrency.ch4

import scala.util.Success
import scala.concurrent.Promise

class IVar[T] {
  private val p = Promise[T]()
  def apply(): T = {
    p.future.value match {
      case Some(Success(t)) => t
      case _ => throw new IllegalStateException("no value!")
    }
  }
  def :=(x:T): Unit = {
    // though not required, as `p success x` would raise an exception if x were already valued, it is a good way to customize the exception
    if(p.isCompleted) throw new IllegalStateException("already valued!")
    else p success x
  }
}

object IVarApp extends App {
  val v:IVar[Int] = new IVar
  try {
    v()
  } catch {
    case e:IllegalStateException => println(e)
  }

  v := 5
  println(v())

  try {
    v := 6
  } catch {
    case e:IllegalStateException => println(e)
  }
}
