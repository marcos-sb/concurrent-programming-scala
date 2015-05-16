package org.concurrency.ch2

object ThreadsProtectedUId extends App {
  var uidCount = 0L
  def getUniqueId() = this.synchronized {
    val freshUid = uidCount + 1
    uidCount = freshUid
    freshUid
  }
}
