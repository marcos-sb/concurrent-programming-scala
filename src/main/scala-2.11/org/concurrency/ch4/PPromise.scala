package org.concurrency.ch4

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

object PPromiseApp extends App {
  implicit class PPromise[T](val p: Promise[T]) {
    def compose[S](f: S => T): Promise[S] = {
      val s = Promise[S]()
      val sf:Future[S] = s.future
      p tryCompleteWith (sf map f)
      s
    }
  }

  val p1 = Promise[Int]()
  val s1:Promise[Int] = p1 compose (_ + 1)

  p1.future foreach println
  s1.future foreach println

  s1 success 1


  val p2 = Promise[Int]()
  val s2:Promise[Int] = p2 compose (_ + 1)

  p2.future onFailure {case e => println(e)}
  s2.future onFailure {case e => println(e)}

  s2 failure new IllegalStateException("I failed on purpose...")


  val p3 = Promise[Int]()
  val s3:Promise[Int] = p3 compose (_ + 1)

  p3.future foreach println
  s3.future foreach println

  p3 success 2
  s3 success 2

  Thread.sleep(1000)
}
