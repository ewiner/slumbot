// from https://gist.github.com/tobnee/5163430

package util

import akka.actor.{ActorSystem, Props, Actor}
import collection.mutable
import play.api.cache.Cache
import concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import play.api.libs.concurrent.Akka._

object AsyncCache {
  import play.api.Play.current

  type AnyFuture = Future[_]

  lazy val cacheRef = system.actorOf(
    Props(new AsyncCacheActor(system, Cache, play.api.Play.current)),
    "asyncCacheActor")

  def setAsync(key: String, get: => AnyFuture, timeout: Int = 0, strategy: UpdateStrategy = LAST_SUCCESS,
               debounce: Long = 0) {
    cacheRef ! AsyncUpdate(key, () => get, timeout, strategy, debounce)
  }

  def getOrElseAsync[E](key: String, timeout: Int = 0, strategy: UpdateStrategy = LAST_SUCCESS,
                        debounce: Long = 0)(orElse: => Future[E]): Future[E] = {
    implicit val to = Timeout(1 minutes)
    Cache.getOrElse[Future[E]](key) {
      (cacheRef ? AsyncUpdate(key, () => orElse, timeout, strategy, debounce)).asInstanceOf[Future[E]]
    }
  }
}

sealed trait UpdateStrategy

case object FIRST_SUCCESS extends UpdateStrategy

case object LAST_SUCCESS extends UpdateStrategy

case object FALLBACK extends UpdateStrategy

case class AsyncUpdate(key: String, get: () => Future[_], timeout: Int, strategy: UpdateStrategy, debounceTime: Long)

class AsyncCacheActor(val actorSystem: ActorSystem, val cache: Cache.type, app: play.api.Application) extends Actor {

  implicit val ec = this.actorSystem.dispatcher
  implicit val playApp = app

  val calledFuturesWithTime = new mutable.HashMap[String, (Long, Future[_])]()

  def receive = {
    case AsyncUpdate(key, get, timeout, strategy, debounceTime) => {
      if (shouldMakeCall(key, debounceTime)) {
        strategy match {
          case FIRST_SUCCESS => {
            val future = get.apply()
            val updateFuture =
              if (futureInProgress(key)) {
                val oldFuture = calledFutures(key)
                val firstComplete = Future.firstCompletedOf(Seq(oldFuture, future))
                val withFallbacks = firstComplete.fallbackTo(oldFuture).fallbackTo(future)
                addCacheHandle(key, withFallbacks, timeout)
              } else {
                future
              }
            updateFutureQueue(key, updateFuture)
          }
          case LAST_SUCCESS => {
            addCacheHandle(key, get.apply(), timeout)
          }
          case FALLBACK => {
            if (futureInProgress(key)) {
              val futureWithFallback = calledFutures(key).recover{case _=> addCacheHandle(key, get.apply(), timeout)}
              updateFutureQueue(key, futureWithFallback)
            } else {
              updateFutureQueue(key, addCacheHandle(key, get.apply(), timeout))
            }
          }
        }
      }
    }
  }

  def updateFutureQueue(key: String, updateFuture: Future[_]) {
    val calltime: Long = System.nanoTime()
    calledFuturesWithTime.put(key, (calltime, updateFuture))
  }

  def calledFutures(key: String) = calledFuturesWithTime(key)._2

  def calledFuturesGet(key: String) = calledFuturesWithTime.get(key).map(_._2)

  def lastCall(key: String) = calledFuturesWithTime.get(key).map(_._1)

  def shouldMakeCall(key: String, debounceTime: Long): Boolean = {
    lastCall(key).filterNot(time => (System.nanoTime() - time) > debounceTime).isDefined
  }

  def futureInProgress(key: String): Boolean = {
    calledFuturesGet(key).filterNot(_.isCompleted).isDefined
  }

  def futureChain(key: String, future: Future[_], timeout: Int, pipe: Future[_] => Future[_]) {
    val f = addCacheHandle(key, future, timeout)
    if (futureInProgress(key)) {
      val futureWithFallback = pipe(calledFutures(key))
      updateFutureQueue(key, futureWithFallback)
    } else {
      updateFutureQueue(key, f)
    }
  }

  def addCacheHandle(key: String, future: Future[_], timeout: Int) = {
    future.onSuccess {
      case value => cache.set(key, value, timeout)
    }
    future
  }
}