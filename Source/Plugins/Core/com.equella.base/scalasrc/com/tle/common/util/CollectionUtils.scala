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

package com.tle.common.util

import scala.jdk.CollectionConverters._

object CollectionUtils {

  /** Computes the intersection of two Scala collections and returns a new List containing elements
    * present in both.
    *
    * Note it will remove the duplicates' element in the return result.
    *
    * @param originalCollection
    *   The original collection (nullable).
    * @param newCollection
    *   The new collection to intersect with (nullable).
    * @return
    *   A new List containing originalCollection ∩ newCollection.
    */
  def intersect(
      originalCollection: Option[Iterable[String]],
      newCollection: Option[Iterable[String]]
  ): Option[Iterable[String]] = {
    (originalCollection, newCollection) match {
      case (None, newOption)         => newOption
      case (original, None)          => original
      case (Some(original), Some(n)) => Option(original.toSet intersect n.toSet).map(_.toList)
      case _                         => None
    }
  }

  /** Computes the intersection of two Java collections and returns a new List containing elements
    * present in both.
    *
    * Note it will remove the duplicates' element in the return result.
    *
    * @param originalCollection
    *   The original collection (nullable).
    * @param newCollection
    *   The new collection to intersect with (nullable).
    * @return
    *   A new List containing originalCollection ∩ newCollection.
    */
  def intersectJava(
      originalCollection: java.util.Collection[String],
      newCollection: java.util.Collection[String]
  ): java.util.Collection[String] = {
    val result =
      intersect(Option(originalCollection).map(_.asScala), Option(newCollection).map(_.asScala))

    result.map(_.toList.asJava).orNull
  }

  /** Converts a Java collection to an Option of Scala List. If the input list is null or empty,
    * returns None.
    */
  def convertEmptyListToNone[T](list: java.util.Collection[T]): Option[List[T]] =
    Option(list).map(_.asScala.toList).filter(_.nonEmpty)

  /** Converts a Scala Iterable to an Option of Scala List. If the input list is null or empty,
    * returns None.
    */
  def convertEmptyListToNone[T](list: Iterable[T]): Option[List[T]] =
    Option(list).map(_.toList).filter(_.nonEmpty)
}
