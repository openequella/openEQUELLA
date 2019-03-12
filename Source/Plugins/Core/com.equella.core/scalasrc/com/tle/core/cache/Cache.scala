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
import java.util.concurrent.ConcurrentHashMap

import cats.data.{Kleisli, StateT}
import cats.effect.{Async, Effect, IO}
import com.tle.core.db.{DB, RunWithDB, UserContext}
import com.tle.core.events.ApplicationEvent
import com.tle.core.events.ApplicationEvent.PostTo
import com.tle.core.events.listeners.ApplicationListener
import com.tle.legacy.LegacyGuice

import scala.concurrent.ExecutionContext.Implicits.global

trait Cacheable[A] {
  def key(userContext: UserContext): String

  def query: DB[A]
}

case class InstCacheable[A](pfx: String, query: DB[A]) extends Cacheable[A] {
  def key(userContext: UserContext): String = userContext.inst.getUniqueId + "_" + pfx
}

case class CacheInvalidationEvent(key: String)
    extends ApplicationEvent[CacheInvalidation](PostTo.POST_TO_OTHER_CLUSTER_NODES) {
  override def getListener: Class[CacheInvalidation] = classOf[CacheInvalidation]

  override def postEvent(listener: CacheInvalidation): Unit = listener.invalidateKey(key)
}

trait CacheInvalidation extends ApplicationListener {
  def invalidateKey(key: String): Unit
}

object Cache extends CacheInvalidation {

  private val concurrentMap = new ConcurrentHashMap[String, IO[Any]]()

  def invalidate[A](implicit c: Cacheable[A]): DB[IO[Unit]] =
    Kleisli { uc: UserContext =>
      StateT.liftF(IO.pure(IO {
        val key = c.key(uc)
        concurrentMap.remove(key)
        LegacyGuice.eventService.publishApplicationEvent(CacheInvalidationEvent(key))
      }))
    }

  def get[A](implicit c: Cacheable[A]): DB[A] = {
    Kleisli { uc: UserContext =>
      StateT.inspectF { s: Connection =>
        val key = c.key(uc)
        concurrentMap
          .computeIfAbsent(key,
                           k =>
                             Async
                               .memoize(
                                 RunWithDB.executeTransaction(uc.ds,
                                                              c.query
                                                                .run(uc)
                                                                .map(IO.pure)))
                               .unsafeRunSync())
          .asInstanceOf[IO[A]]
      }
    }
  }

  override def invalidateKey(key: String): Unit = concurrentMap.remove(key)

}
