package org.concurrency.ch4

import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

object FFutureApp extends App {
  implicit class FFuture[T](val s: Future[T]) {
    def exists(p: T => Boolean): Future[Boolean] =
      Future {
        s.value match {
          case Some(Success(x)) => p(x)
          case _ => false
        }
      }
  }

  val l = List(Future {
    5
  }.exists(_ > 5), //always false

  Promise[Int]().failure(new IllegalStateException("something bad happened!")).future.exists(_ > 5), //always false

  Future {
    6
  }.exists(_ > 5), //true iif future completes before exists (most of the times)

  Future {
    Thread.sleep(2000)
    6
  }.exists(_ > 5) //false unless...
  )

  l foreach {_ foreach println}
}
