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

package com.tle.web.remoting.graphql

import com.tle.common.beans.exception.NotFoundException

package object provider {

  /** A utility method to wrap a function that may throw a `NotFoundException` and return an
    * `Option`. If the function throws a `NotFoundException`, it returns `None`, otherwise it
    * returns `Some(value)`.
    *
    * @param fn
    *   the function to execute
    * @tparam T
    *   the type of the value returned by the function
    * @return
    *   an `Option[T]` that is `None` if the function throws a `NotFoundException`, otherwise `Some`
    *   with the result of the function
    */
  def noneIfNotFound[T](fn: => T): Option[T] = {
    try {
      Option(fn)
    } catch {
      case _: NotFoundException => None
    }
  }
}
