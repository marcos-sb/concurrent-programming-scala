package org.concurrency.ch6

import rx.lang.scala.Observable

import scala.util.Random

object Quoter {
  def randomQuote = Observable[String](
    subscriptor => {
      //iheartquote is down...
      subscriptor.onNext(Random.nextString(Random.nextInt(50)))
      subscriptor.onCompleted()
    }
  )

  def movingAverageObs:Observable[Double] = {
    var avg:Double = 0.0d
    var elCount:Long = 0l

    val o = randomQuote.map(n => {
      avg = (avg*elCount+n.length)/(elCount+1)
      elCount +=1; avg
    })
    o
  }
}

object QuoterApp extends App {
  val quoter:Observable[Double] = Quoter.movingAverageObs
  for(_ <- 0 until 200)
    quoter.subscribe()
  quoter.subscribe(println(_))
  //the printed result should be around 25, which is the expected length of the random strings
}
