package org.concurrency.ch3

import scala.collection.concurrent.Map
import scala.collection.mutable

class SyncConcurrentMap[A,B] extends Map[A,B] {

  private final val lock: AnyRef = new AnyRef
  private final val helperMap = mutable.HashMap[A, B]()

  override def replace(k: A, v: B): Option[B] = lock.synchronized {
    helperMap get k match {
      case Some(_) => helperMap put (k,v)
      case None => None
    }
  }

  override def replace(k: A, oldvalue: B, newvalue: B): Boolean = lock.synchronized {
    helperMap get k match {
      case Some(vfromk) if vfromk == oldvalue =>
          helperMap put (k,newvalue); true

      case _ => false
    }
  }

  override def remove(k: A, v: B): Boolean = lock.synchronized {
    helperMap get k match {
      case Some(vfromk) if v == vfromk => helperMap remove k; true
      case _ => false
    }
  }

  override def putIfAbsent(k: A, v: B): Option[B] = lock.synchronized {
    helperMap get k match {
      case Some(_) => None
      case None => helperMap put (k,v)
    }
  }

  override def +=(kv: (A, B)): this.type = lock.synchronized {
    helperMap put (kv._1, kv._2); this
  }

  override def -=(key: A): this.type = lock.synchronized {
    helperMap remove key; this
  }

  override def get(key: A): Option[B] = lock.synchronized {
    helperMap get key
  }

  override def iterator: Iterator[(A, B)] = helperMap.iterator

}

// as every method besides iterator is protected by a synchronized, there is little interest in assessing SyncConcurrentMap's ability to execute concurrently
object SyncConcurrentMapApp extends App {
  val scm = new SyncConcurrentMap[Int,Int]
  scm += ((1,2))
  scm put (2,3)
  scm foreach println
}