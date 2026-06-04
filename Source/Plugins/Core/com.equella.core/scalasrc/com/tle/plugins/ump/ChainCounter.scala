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

import java.util.concurrent.Executors
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import java.util.function.ToIntFunction
import scala.jdk.CollectionConverters._

/** Counts matching entries across a [[UserDirectory]] plugin chain concurrently.
  *
  * Queries all plugins in parallel and sums their counts.
  *
  * @param chain
  *   the ordered list of user directory plugins to query
  */
class ChainCounter(chain: java.util.List[UserDirectory]) {

  private val virtualThreadEC: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newVirtualThreadPerTaskExecutor())

  /** Returns the total number of matching entries across all plugins in the chain.
    *
    * All plugins are queried concurrently; the individual counts are then summed.
    *
    * @param countFunction
    *   returns the number of matching entries for a given plugin
    * @return
    *   the sum of each plugin's count
    */
  def count(countFunction: ToIntFunction[UserDirectory]): Int = {
    implicit val ec: ExecutionContext = virtualThreadEC

    val totalFuture = Future
      .traverse(chain.asScala) { directory =>
        Future(countFunction.applyAsInt(directory))
      }
      .map(_.sum)

    Await.result(totalFuture, 30.seconds)
  }
}
