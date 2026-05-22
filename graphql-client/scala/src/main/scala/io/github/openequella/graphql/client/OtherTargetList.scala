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

object OtherTargetList {

  /** Discriminator indicating what kind of target this list is for.
    */
  def targetType: SelectionBuilder[OtherTargetList, OtherTargetListType] =
    _root_.caliban.client.SelectionBuilder.Field("targetType", Scalar())

  /** For ITEM_STATUS targets: the item status this list applies to (e.g. DRAFT, LIVE, MODERATING).
    * Null for other target types.
    */
  def itemStatus: SelectionBuilder[OtherTargetList, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("itemStatus", OptionOf(Scalar()))

  /** For ITEM_METADATA targets: the ID of the metadata rule this list applies to. Null for other
    * target types.
    */
  def metadataRuleId: SelectionBuilder[OtherTargetList, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("metadataRuleId", OptionOf(Scalar()))

  /** For WORKFLOW_TASK targets: the ID of the workflow task this list applies to. Null for other
    * target types.
    */
  def taskId: SelectionBuilder[OtherTargetList, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("taskId", OptionOf(Scalar()))

  /** The access control entries for this target.
    */
  def entries[A](
      innerSelection: SelectionBuilder[TargetListEntry, A]
  ): SelectionBuilder[OtherTargetList, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field("entries", ListOf(Obj(innerSelection)))
}
