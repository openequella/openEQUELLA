/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.plugins.ump

import com.tle.plugins.ump.UserDirectory.ChainResult

import java.util

/** A typed replacement for the legacy `Pair[ChainResult, Collection[T]]` that was previously used
  * as the return type of [[UserDirectory]] search and lookup methods.
  *
  * Carries both the [[ChainResult]] directive (whether the plugin chain should continue or stop)
  * and the collection of results returned by a [[UserDirectory]] operation.
  *
  * Use the factory methods in the companion object to construct instances. The `results` field
  * should not be `null`; use [[ChainedResult.continueWithEmpty]] when there are no results.
  *
  * @param chainResult
  *   whether the plugin chain should continue or stop after this result
  * @param results
  *   the entities returned by this plugin
  * @tparam T
  *   the type of elements in the result collection
  */
final class ChainedResult[T](val chainResult: ChainResult, val results: util.List[T])

object ChainedResult {

  /** Returns a [[ChainedResult]] that stops the chain and carries the given results. */
  def stopWith[T](results: util.List[T]): ChainedResult[T] =
    new ChainedResult[T](ChainResult.STOP, results)

  /** Returns a [[ChainedResult]] that continues the chain and carries the given results. */
  def continueWith[T](results: util.List[T]): ChainedResult[T] =
    new ChainedResult[T](ChainResult.CONTINUE, results)

  /** Returns a [[ChainedResult]] that continues the chain with an empty result collection. Prefer
    * this over passing `null` when a plugin has no results to contribute.
    */
  def continueWithEmpty[T](): ChainedResult[T] =
    continueWith[T](util.List.of[T]())
}
