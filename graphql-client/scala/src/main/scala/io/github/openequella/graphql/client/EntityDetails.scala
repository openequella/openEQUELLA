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

object EntityDetails {

  /** Unique identifier for this entity.
    */
  def id: SelectionBuilder[EntityDetails, Long] =
    _root_.caliban.client.SelectionBuilder.Field("id", Scalar())

  /** Universally unique identifier for this entity, used for referencing across different entities.
    */
  def uuid: SelectionBuilder[EntityDetails, String] =
    _root_.caliban.client.SelectionBuilder.Field("uuid", Scalar())

  /** Owner of this entity, typically the user who created it.
    */
  def owner: SelectionBuilder[EntityDetails, String] =
    _root_.caliban.client.SelectionBuilder.Field("owner", Scalar())

  /** Timestamp for when this entity was created - only valid when starting an edit on an existing
    * entity.
    */
  def dateCreated: SelectionBuilder[EntityDetails, scala.Option[java.time.LocalDateTime]] =
    _root_.caliban.client.SelectionBuilder.Field("dateCreated", OptionOf(Scalar()))

  /** Timestamp for when this entity was last modified - only valid when starting an edit on an
    * existing entity.
    */
  def dateModified: SelectionBuilder[EntityDetails, scala.Option[java.time.LocalDateTime]] =
    _root_.caliban.client.SelectionBuilder.Field("dateModified", OptionOf(Scalar()))

  /** Language bundle for the _name_ of this entity, used for internationalization.
    */
  def nameBundle[A](
      innerSelection: SelectionBuilder[LanguageBundle, A]
  ): SelectionBuilder[EntityDetails, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field("nameBundle", OptionOf(Obj(innerSelection)))

  /** Language bundle for the _description_ of this entity, used for internationalization.
    */
  def descriptionBundle[A](
      innerSelection: SelectionBuilder[LanguageBundle, A]
  ): SelectionBuilder[EntityDetails, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field("descriptionBundle", OptionOf(Obj(innerSelection)))

  /** A map of additional attributes for this entity, allowing for flexible metadata storage.
    */
  def attributes[A](
      innerSelection: SelectionBuilder[KVStringString, A]
  ): SelectionBuilder[EntityDetails, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field("attributes", ListOf(Obj(innerSelection)))

  /** Indicates whether this entity is disabled, affecting its visibility and usability.
    */
  def disabled: SelectionBuilder[EntityDetails, Boolean] =
    _root_.caliban.client.SelectionBuilder.Field("disabled", Scalar())
}
