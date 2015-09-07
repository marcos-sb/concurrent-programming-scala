package org.concurrency.ch6

import java.io.IOException

import rx.lang.scala.{Subject, Observable}
import scala.concurrent.duration._

import java.nio.file.{Paths, Files}

object FileOps {
  def copyFile(src: String, dst: String): Observable[Double] = {
    val srcp = Paths.get(src)
    val dstp = Paths.get(dst)
    val sz = Files.size(srcp).toDouble

    val su = Subject[Double]()
    val sub = Observable.interval(100.millis).map(_ => {
      try {
        Files.size(dstp) / sz
      } catch {
        case ioe: IOException => 0
      }
    }).subscribe(su)

    val t = new Thread(new Runnable {
      def run() {
        Files.copy(srcp, dstp)
      }
    })
    t.setUncaughtExceptionHandler(
      new Thread.UncaughtExceptionHandler() {
        def uncaughtException(th: Thread, ex: Throwable) {
          su.onError(ex)
        }
      }
    )

    t.start()

    su.doOnNext(v => if(v >= 1d) {
      su.onCompleted()
      sub.unsubscribe()
    })
  }
}

object FileOpsApp extends App {
  FileOps.copyFile("/Users/marcos/Downloads/LibreOffice_5.0.1_MacOS_x86-64.dmg", "/Users/marcos/Downloads/test/LibreOffice").subscribe(
    println(_), println(_), () => println("done!")
  )

  //try running it twice... ;)

  Thread.sleep(5000)
}