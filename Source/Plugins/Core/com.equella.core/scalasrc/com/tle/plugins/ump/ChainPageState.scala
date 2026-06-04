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

/** Immutable state accumulator for a single pagination pass. Completely decoupled from external
  * directories and execution mechanics.
  *
  * @param results
  *   Accumulated page items. Uses [[Vector]] because its 32-ary trie structure gives O(log₃₂ n) —
  *   effectively near-constant — concatenation, which is significantly faster than the O(n) `++` on
  *   [[List]].
  * @param remainingLimit
  *   Number of items still needed to fill the page.
  * @param remainingOffset
  *   Number of items still to skip before the page window begins.
  * @param stopped
  *   `true` once a plugin has signalled [[ChainDirective#STOP]], preventing any further plugins
  *   from being queried.
  */
case class ChainPageState[T](
    results: Vector[T] = Vector.empty,
    remainingLimit: Int,
    remainingOffset: Int,
    stopped: Boolean = false
) {

  /** `true` when no more items can be added to the page or the chain was stopped by a plugin. */
  def isFullOrStopped: Boolean = remainingLimit <= 0 || stopped

  /** Returns true when all items in the current directory are before the requested page window.
    *
    * @param currentDirectoryItemCount
    *   the total number of matching items in the current directory
    */
  def shouldSkip(currentDirectoryItemCount: Int): Boolean =
    remainingOffset >= currentDirectoryItemCount

  /** Records that a whole directory has been skipped.
    *
    * @param numberOfItemsToSkip
    *   the number of items in the skipped directory.
    */
  def skip(numberOfItemsToSkip: Int): ChainPageState[T] =
    copy(remainingOffset = remainingOffset - numberOfItemsToSkip)

  /** Merges a plugin's result slice into the accumulator.
    *
    * @param newElements
    *   new items to add to the page, typically fetched via `pageFunction`
    * @param isStopDirective
    *   `true` if the plugin signalled [[ChainDirective#STOP]], preventing further plugins from
    *   being queried
    */
  def addResult(newElements: Vector[T], isStopDirective: Boolean): ChainPageState[T] =
    copy(
      results = results ++ newElements,
      remainingLimit = remainingLimit - newElements.size,
      remainingOffset = 0,
      stopped = isStopDirective
    )
}
