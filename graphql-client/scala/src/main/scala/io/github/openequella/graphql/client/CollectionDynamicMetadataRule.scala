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

package io.github.openequella.graphql.client

import caliban.client.FieldBuilder._
import caliban.client._

object CollectionDynamicMetadataRule {

  /** Unique identifier for this rule
    */
  def ruleId: SelectionBuilder[CollectionDynamicMetadataRule, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("ruleId", OptionOf(Scalar()))

  /** Display name for this rule
    */
  def name: SelectionBuilder[CollectionDynamicMetadataRule, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("name", OptionOf(Scalar()))

  /** XPath to the metadata node whose values drive the ACL
    */
  def path: SelectionBuilder[CollectionDynamicMetadataRule, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("path", OptionOf(Scalar()))

  /** Type of dynamic rule
    */
  def ruleType: SelectionBuilder[CollectionDynamicMetadataRule, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("ruleType", OptionOf(Scalar()))

  /** ACL entries applied by this rule
    */
  def targetList[A](
      innerSelection: SelectionBuilder[TargetListEntry, A]
  ): SelectionBuilder[CollectionDynamicMetadataRule, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field("targetList", ListOf(Obj(innerSelection)))
}
