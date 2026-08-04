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

import scala.jdk.CollectionConverters._

/** Pages over a [[UserDirectory]] plugin chain, returning a single page of results.
  *
  * @param chain
  *   the ordered list of user directory plugins to query
  * @param pagingFunctions
  *   the counting and paging callbacks for each plugin
  * @tparam T
  *   the type of entity being paged
  */
class ChainPager[T](
    chain: java.util.List[UserDirectory],
    pagingFunctions: PagingFunctions[T]
) {

  /** Returns the page of results described by `pageRange`.
    *
    * @param pageRange
    *   the requested offset and limit
    * @return
    *   the assembled page as a Java List
    */
  def getPage(pageRange: PageRange): java.util.List[T] = {
    val initialState = ChainPageState[T](
      remainingLimit = pageRange.limit,
      remainingOffset = pageRange.offset
    )

    val finalState = ChainPageEngine.processChain(
      directories = chain.asScala.toList,
      initialState = initialState,
      pagingFunctions = pagingFunctions
    )

    finalState.results.toList.asJava
  }
}
