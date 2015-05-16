package org.concurrency.ch2

import ThreadsProtectedUId.getUniqueId

object SynchronizedAvoidedDeadlock extends App {
  class Account(val name: String, var money: Int) {
    val uid = getUniqueId()
  }

  def send(a1:Account, a2:Account, n:Int) = {
    def adjust() {
      a1.money -= n
      a2.money += n
    }
    if(a1.uid < a2.uid) a1.synchronized {
      a2.synchronized {
        adjust() }
    } else a2.synchronized {
      a1.synchronized {
        adjust() }
    }
  }
}
