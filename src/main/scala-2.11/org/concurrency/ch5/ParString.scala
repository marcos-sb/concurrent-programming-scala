package org.concurrency.ch5

import scala.collection.parallel.{SeqSplitter, immutable}

class ParStringSplitter (val s: String, var i: Int, val limit: Int) extends SeqSplitter[Char] {
  final def hasNext = i < limit
  final def next() = {
    val r = s.charAt(i)
    i += 1
    r
  }
  def dup = new ParStringSplitter(s, i, limit)
  def remaining = limit - i
  def split = {
    val rem = remaining
    if (rem >= 2) psplit(rem / 2, rem - rem / 2)
    else Seq(this)
  }
  def psplit(sizes: Int*): Seq[ParStringSplitter] = {
    val ss = for (sz <- sizes) yield {
      val nlimit = (i + sz) min limit
      val ps = new ParStringSplitter(s, i, nlimit)
      i = nlimit
      ps
    }
    if (i == limit) ss
    else ss :+ new ParStringSplitter(s, i, limit)
  }
}

class ParString (val str: String) extends immutable.ParSeq[Char] {
  def apply(i: Int) = str.charAt(i)
  def length = str.length
  def splitter = new ParStringSplitter(str, 0, str.length)
  def seq = new collection.immutable.WrappedString(str)
}
