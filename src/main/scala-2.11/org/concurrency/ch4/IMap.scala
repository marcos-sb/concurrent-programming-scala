package org.concurrency.ch4

import scala.collection.concurrent
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class IMap[K, V] {
  private val map:concurrent.Map[K, Promise[V]] = new ConcurrentHashMap[K, Promise[V]]()

  def update(k: K, v: V): Unit = {
    val promise = map get k
    promise match {
      case None => map += (k -> (Promise[V]() success v))
      case Some(prom) => prom success v // catch exc...
    }
  }

  def apply(k:K): Future[V] = {
    map get k match {
      case None => {
        val p = Promise[V]()
        map += (k -> p)
        p future
      }
      case Some(prom) => prom future
    }
  }
}


object IMapApp extends App {
  val m = new IMap[Int, Int]()

  m(1) foreach println

  try {
    m update(1,11)
    // previous println
    m update(1,2)
  } catch {
    case e:IllegalStateException => println(e)
  }

  Thread.sleep(1000)
}
