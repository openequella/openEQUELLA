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

package io.github.openequella.graphql

package object api {

  /** Flattens the result of an operation that returns an `Either[List[ApiError], Option[A]]` to an
    * `Either[List[ApiError], A]`. This is needed because Caliban returns an `Option[A]` for
    * operations that return an effect representing any operation which can fail.
    *
    * See more at:
    * <https://ghostdogpr.github.io/caliban/faq/#the-auto-generated-schema-shows-a-field-is-nullable-but-i-want-it-non-nullable-instead>
    */
  def flattenResult[A](result: Either[List[ApiError], Option[A]]): Either[List[ApiError], A] =
    result.flatMap {
      case Some(a) => Right(a)
      case None => Left(List(UnknownError("Although operation successful, no data was returned.")))
    }
}
