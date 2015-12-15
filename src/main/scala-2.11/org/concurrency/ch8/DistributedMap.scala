package org.concurrency.ch8

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._


object ShardActor {
  case class Update(key: Any, value: Any)
  case class Get(key: Any)
}

class ShardActor extends Actor {
  private val logger = Logging(context.system, this)
  private val hash = mutable.HashMap[Any,Any]()

  override def receive = {
    case ShardActor.Update(k,v) =>
      logger.info(s"received Update($k,$v)")
      sender() ! hash.update(k,v)

    case ShardActor.Get(k) =>
      logger.info(s"received Get($k)")
      sender() ! hash.get(k)
  }
}

class DistributedMap[K,V] (shards: ActorRef*) {

  private val mask:Int = { //extract lower-end bits || really needed???
    var tmpLength = shards.length
    var position = 1
    tmpLength >>>= 1
    while(tmpLength > 0) {
      tmpLength >>>= 1
      position += 1
    }
    (1 << position) - 1
  }

  private val timeout:Timeout = new Timeout(4.seconds)
  private def getShard(key: K): ActorRef =
    shards((key.hashCode() & mask) % shards.length) // ???? vs. next line
    //shards(key.hashCode() % shards.length)

  def update(key: K, value: V): Future[Unit] =
    getShard(key).ask(ShardActor.Update(key,value))(timeout).mapTo[Unit]

  def get(key: K): Future[Option[V]] =
    getShard(key).ask(ShardActor.Get(key))(timeout).mapTo[Some[V]]

}

object DistributedMapApp extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val actorSystem = ActorSystem("DMAS")

  val sh1 = actorSystem.actorOf(Props[ShardActor], name="sh1")
  val sh2 = actorSystem.actorOf(Props[ShardActor], name="sh2")

  val dm = new DistributedMap[Int, String](sh1,sh2)

  //`1` goes to sh2
  //`2` goes to sh1
  dm.update(1,"One")
  dm.update(2,"Two")

  dm.get(2).onSuccess {
    case Some(value) => println(value)
    case None => println("no key found")
    case _ =>
  }

  Thread.sleep(4000)

  actorSystem.terminate()
}
