package org.concurrency.ch4

import java.util.Timer
import java.util.TimerTask

import scala.io.Source
import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object Curly extends App {

  private val timer = new Timer(true)

  def timeout(t:Int): Future[Unit] = {
    val p = Promise[Unit]()
    timer.schedule(new TimerTask {
      override def run() = {
        p success
        timer.cancel()
      }
    }, t)
    p.future
  }

  val fetcher = Promise[String]()
  val url = Source.stdin.getLines().next()

  Future[Unit] {
    val f = timeout(2000)
    val f2 = fetcher.future
    while (!f.isCompleted && !f2.isCompleted) {
      Thread.sleep(50)
      print(".")
    }
  }

  fetcher success Source.fromURL(url).mkString

  fetcher.future foreach {
    case html => println(html)
  }

  Thread.sleep(3000)
}
