package org.concurrency.ch2

import scala.collection._
import org.concurrency.ch2.Thread.thread

object SynchronizedNesting extends App {
  private val transfers = mutable.ArrayBuffer[String]()

  def logTransfer(name: String, n: Int) = transfers.synchronized {
    transfers += s"transfer to account '$name' = $n"
  }

  class Account(val name: String, var money: Int)

  def add(account: Account, n: Int) = account.synchronized {
    account.money += n
    if (n > 10) logTransfer(account.name, n)
  }

  val jane = new Account("Jane", 100)
  val john = new Account("John", 200)
  val t1 = thread {
    add(jane, 5)
  }
  val t2 = thread {
    add(john, 50)
  }
  val t3 = thread {
    add(jane, 70)
  }
  t1.join(); t2.join(); t3.join()
  println(s"--- transfers ---\n$transfers")
}
