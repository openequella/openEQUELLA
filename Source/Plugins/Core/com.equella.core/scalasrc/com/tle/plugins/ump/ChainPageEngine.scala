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

import scala.jdk.CollectionConverters.CollectionHasAsScala

/** Engine for processing a single page request across a [[UserDirectory]] plugin chain.
  */
object ChainPageEngine {

  /** Folds over the plugin chain to process a single page request, returning the final
    * [[ChainPageState]].
    *
    * @param directories
    *   the ordered list of plugins to process
    * @param initialState
    *   the initial state of the page accumulator, typically with an empty result set and the
    *   requested limit and offset
    * @param pagingFunctions
    *   the counting and paging callbacks for each plugin
    */
  def processChain[T](
      directories: List[UserDirectory],
      initialState: ChainPageState[T],
      pagingFunctions: PagingFunctions[T]
  ): ChainPageState[T] = {
    directories.foldLeft(initialState) { (state, userDirectory) =>
      processDirectory(userDirectory, state, pagingFunctions)
    }
  }

  // Processes a single plugin for the current page request.
  private def processDirectory[T](
      directory: UserDirectory,
      state: ChainPageState[T],
      pagingFunctions: PagingFunctions[T]
  ): ChainPageState[T] = {
    if (state.isFullOrStopped) {
      state // Short-circuits further processing - page is full or chain was stopped.
    } else {
      processActiveDirectory(directory, state, pagingFunctions)
    }
  }

  // Processes a single plugin that may contribute to the current page request.
  private def processActiveDirectory[T](
      directory: UserDirectory,
      state: ChainPageState[T],
      pagingFunctions: PagingFunctions[T]
  ): ChainPageState[T] = {
    val directoryCount = pagingFunctions.countFunction.applyAsInt(directory)

    if (state.shouldSkip(directoryCount)) {
      state.skip(directoryCount)
    } else {
      fetchAndAccumulatePage(directory, state, pagingFunctions.pageFunction)
    }
  }

  // Fetches a page slice from the plugin and merges it into the accumulator.
  private def fetchAndAccumulatePage[T](
      directory: UserDirectory,
      state: ChainPageState[T],
      pageFunction: PageFunction[T]
  ): ChainPageState[T] = {
    val newResult = pageFunction.fetch(directory, state.remainingLimit, state.remainingOffset)

    state.addResult(
      newElements = newResult.results.asScala.toVector,
      isStopDirective = newResult.chainDirective == ChainDirective.STOP
    )
  }
}
