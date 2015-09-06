package org.concurrency.ch6

import rx.lang.scala.{Subject, Observable}
import rx.lang.scala.subjects.PublishSubject
import scala.collection.mutable.HashMap

class RMap[K, V] {
  private val subjects = HashMap[K, Subject[V]]()
  private val k2v = HashMap[K,V]()

  def update(k: K, v: V): Unit = {
    k2v.update(k,v)
    if(subjects.contains(k)) {
      val su = subjects(k)
      su.onNext(v)
    }
  }

  def apply(k: K): Observable[V] = {
    if(subjects contains k) subjects(k)
    else {
      val ps = PublishSubject[V]()
      subjects += (k -> ps)
      ps
    }
  }
}

object RMapApp extends App {
  val rmap = new RMap[String, Int]()
  rmap update ("one", 1) //shouldn't print

  rmap("one").subscribe(println(_))
  rmap update ("one", 11) //should print 11

  rmap("one").subscribe(println(_))
  rmap update ("one", 111) //should print 111\n111

  rmap("two").subscribe(println(_))
  rmap update ("two", 2) //should print 2
}