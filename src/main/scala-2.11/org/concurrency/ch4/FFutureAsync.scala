package org.concurrency.ch4

import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success
import scala.async.Async.async


object FFutureAsync extends App {
  implicit class FFuture[T](val s: Future[T]) {
    def exists(p: T => Boolean): Future[Boolean] =
      async {
        s.value match {
          case Some(Success(x)) => p(x)
          case _ => false
        }
      }
  }

  val l = List(async {
    5
  }.exists(_ > 5), //always false

  Promise[Int]().failure(new IllegalStateException("something bad happened!")).future.exists(_ > 5), //always false

  async {
    6
  }.exists(_ > 5), //true iif the future completes before exists (most of the times)

  async {
    Thread.sleep(2000)
    6
  }.exists(_ > 5) //false unless...
  )

  l foreach {_ foreach println}

  Thread.sleep(1000)
}
