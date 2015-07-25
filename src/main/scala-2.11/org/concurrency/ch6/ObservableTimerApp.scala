package org.concurrency.ch6

import rx.lang.scala.Observable
import scala.concurrent.duration._

object ObservableTimerApp extends App {
  def observableTimer(): Observable[Long] = {
    Observable(
      subscriptor => {
        Observable.interval(5.seconds).filter(n => (n * 5) % 30 != 0).subscribe {
          n => subscriptor.onNext(n)
        }
        Observable.interval(12.seconds).filter(n => (n * 12) % 30 != 0).subscribe {
          n => subscriptor.onNext(n)
        }
      }
    )
  }

  observableTimer().subscribe(println(_))

  Thread.sleep(400000)
}
