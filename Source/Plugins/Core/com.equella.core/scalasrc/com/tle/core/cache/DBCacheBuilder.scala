/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.cache

import java.sql.Connection
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import cats.data.{Kleisli, StateT}
import cats.effect.{Async, IO}
import com.tle.core.db._
import com.tle.core.events.ApplicationEvent
import com.tle.core.events.ApplicationEvent.PostTo
import com.tle.core.events.listeners.ApplicationListener
import com.tle.legacy.LegacyGuice
import cats.syntax.applicative._

import scala.collection.mutable

trait Cacheable[K, V] {
  def cacheId: String

  def key(userContext: UserContext, v: K): String

  def query: K => DB[V]
}

case class InstCacheable[V](cacheId: String, query: Unit => DB[V]) extends Cacheable[Unit, V] {
  def key(userContext: UserContext, v: Unit): String = userContext.inst.getUniqueId.toString
}

case class CacheInvalidationEvent(cacheId: String, key: String)
    extends ApplicationEvent[CacheInvalidation](PostTo.POST_TO_OTHER_CLUSTER_NODES) {
  override def getListener: Class[CacheInvalidation] = classOf[CacheInvalidation]

  override def postEvent(listener: CacheInvalidation): Unit = listener.invalidateKey(cacheId, key)
}

trait CacheInvalidation extends ApplicationListener {
  def invalidateKey(cacheId: String, key: String): Unit
}

trait Cache[K, V] {
  def invalidate: K => DB[IO[Unit]]
  def get: K => DB[V]
  def getIfValid(k: K, valid: V => Boolean): DB[V] = get(k).flatMap { value =>
    if (valid(value)) value.pure[DB]
    else invalidate(k).flatMap(dbLiftIO.liftIO).flatMap(_ => get(k))
  }
}

object DBCacheBuilder extends CacheInvalidation {

  val globalCaches: mutable.Map[String, ConcurrentMap[String, AnyRef]] = mutable.Map()

  def buildCache[K, V](cacheable: Cacheable[K, V],
                       backingMap: ConcurrentMap[String, AnyRef] =
                         new ConcurrentHashMap[String, AnyRef]()): Cache[K, V] = {
    globalCaches.put(cacheable.cacheId, backingMap)
    new Cache[K, V] {
      def invalidate: K => DB[IO[Unit]] =
        (k: K) =>
          Kleisli { uc: UserContext =>
            StateT.liftF {
              IO.pure {
                IO {
                  val key = cacheable.key(uc, k)
                  backingMap.remove(key)
                  LegacyGuice.eventService.publishApplicationEvent(
                    CacheInvalidationEvent(cacheable.cacheId, key))
                }
              }
            }
        }

      def get: K => DB[V] =
        (k: K) =>
          Kleisli { uc: UserContext =>
            StateT.inspectF { s: Connection =>
              val key = cacheable.key(uc, k)
              backingMap
                .computeIfAbsent(
                  key,
                  _ =>
                    Async
                      .memoize(
                        RunWithDB.executeTransaction(uc.ds,
                                                     cacheable.query(k).run(uc).map(IO.pure)))
                      .unsafeRunSync())
                .asInstanceOf[IO[V]]
            }
        }
    }
  }

  override def invalidateKey(cacheId: String, key: String): Unit = {
    globalCaches.get(cacheId).foreach(_.remove(key))
  }
}
