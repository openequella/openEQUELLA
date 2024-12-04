package com.tle.core.replicatedcache

import com.google.common.base.Optional
import com.tle.common.Pair
import com.tle.core.replicatedcache.ReplicatedCacheService.ReplicatedCache

import java.lang
import scala.collection.concurrent.TrieMap

/** A implementation of `ReplicatedCache` for testing which is _not_ replicated and simply stores
  * things in a TriMap instance.
  *
  * @tparam T
  *   the type of data to be stored
  */
class TrieMapCache[T <: Serializable] extends ReplicatedCache[T] {
  private val cache: TrieMap[String, T] = TrieMap()

  override def get(key: String): Optional[T] = cache.get(key) match {
    case Some(value) => Optional.of(value)
    case None        => Optional.absent()
  }

  override def put(key: String, value: T): Unit = cache.addOne(key, value)

  override def invalidate(keys: String*): Unit = keys.foreach(cache.remove)

  override def iterate(keyPrefixFilter: String): lang.Iterable[Pair[String, T]] = ???
}
