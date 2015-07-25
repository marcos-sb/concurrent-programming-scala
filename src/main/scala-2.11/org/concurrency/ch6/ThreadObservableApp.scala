package org.concurrency.ch6

import rx.lang.scala.Observable
import scala.concurrent.duration._

object ThreadObservableApp extends App {

  def observeThread(t:Thread, d:Duration = 1.second): Observable[Thread] = {
    Observable(subscriber => {
      Observable.interval(d).subscribe(_ => {
        if(t.isAlive) {
          subscriber.onNext(t)
          subscriber.onCompleted()
        }
      })
    })
  }

  val t = new Thread {
    override def run() = {
      println("I'm a thread... I'm running")
      Thread.sleep(2000)
    }
  }

  observeThread(t, 0.5.second).subscribe(t => println(s"${t.getName} started"))

  t.start()
  Thread.sleep(3000)
}
