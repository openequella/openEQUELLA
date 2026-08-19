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

import java.util.function.ToIntFunction

/** A value object that bundles the two paging callbacks needed to query a [[UserDirectory]] plugin.
  *
  * @param countFunction
  *   returns the total number of matching items for a given plugin
  * @param pageFunction
  *   fetches a result slice `(plugin, limit, offset)` from a given plugin
  * @tparam T
  *   the type of entity being paged
  */
case class PagingFunctions[T](
    countFunction: ToIntFunction[UserDirectory],
    pageFunction: PageFunction[T]
)
