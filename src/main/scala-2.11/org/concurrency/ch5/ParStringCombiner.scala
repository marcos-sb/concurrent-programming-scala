package org.concurrency.ch5

import scala.collection.parallel.Combiner
import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.mutable.ParArray

class ParStringCombiner extends Combiner[Char, ParString] {
  private val chunks = new ArrayBuffer += new StringBuilder
  private var sz = 0
  private var lastc = chunks.last

  override def size:Int = sz

  override def += (elem: Char) = {
    lastc += elem
    sz += 1
    this
  }

  override def clear(): Unit = {
    chunks.clear()
    chunks += new StringBuilder
    lastc = chunks.last
    sz = 0
  }

  override def combine[U <: Char, NewTo >: ParString](that: Combiner[U, NewTo]):Combiner[U, NewTo] = {
    if(this eq that) this
    else that match {
      case that: ParStringCombiner => sz += that.size
      chunks ++= that.chunks
      lastc = chunks.last
      this
    }
  }

  override def result(): ParString = {

    // //try #1 'manual' pallelizing
    // //worst performant of the three
    //
    // val resArr = new Array[Char](this.size)
    // val accSz = new ArrayBuffer[Int]
    //
    //  //calculate accumulate lengths so that I can concat chars in parallel afterwards
    // var tmpSz = 0
    // accSz += 0
    // for(sb <- chunks) {
    //   accSz += sb.length + tmpSz
    //   tmpSz += sb.length
    // }
    //
    // for (i <- chunks.indices.par) {
    //   val sb = chunks(i)
    //   val shift = accSz(i)
    //   for(j <- sb.indices) resArr(shift + j) = sb(j)
    // }
    //
    // new ParString(resArr.mkString)

    // // try #2 String aggregate
    // new ParString(chunks.par.aggregate("") (
    //   (acc, line) => acc + line,
    //   (acc1, acc2) => acc1 + acc2
    // ))

    // // try #3 StringBuilder aggregate
    // new ParString(chunks.par.aggregate(new StringBuilder) (
    //   (acc,line) => acc.append(line),
    //   (acc1, acc2) => acc1.append(acc2)
    // ).toString())

    // try #4 sequential result
    val rsb = new StringBuilder
    for(sb <- chunks) rsb.append(sb)
    new ParString(rsb.toString())
  }
}

object ParStringCombinerApp extends App {
  val valpartxt = new ParString("Get rid of those superfluous whitespaces, please" * 3)
  println(valpartxt.filter(_ != ' '));

  val txt = "A custom txt" * 25000
  val partxt = new ParString(txt)
  val seqtime = Timer.warmedTimed(250,2) {txt.filter(_ != ' ')}
  val partime = Timer.warmedTimed(250,2) {partxt.filter(_ != ' ')}

  println(s"seq: $seqtime ms")
  println(s"par: $partime ms")

}
