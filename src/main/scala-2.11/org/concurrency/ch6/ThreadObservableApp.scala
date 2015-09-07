package org.concurrency.ch6

import rx.lang.scala.Observable
import scala.concurrent.duration._

object ThreadObservableApp extends App {

  def observeThread(t:Thread, d:Duration = 1.second): Observable[Thread] = {
    Observable.interval(d).filter(_ => t.isAlive).map(_ => t).first
  }

  val t = new Thread {
    override def run() = {
      println("I'm a thread... I'm running")
      Thread.sleep(2000)
    }
  }

  observeThread(t, 0.5.second).subscribe(t => println(s"${t.getName} started"))

  Thread.sleep(1000)
  t.start()
  Thread.sleep(3000)
}
