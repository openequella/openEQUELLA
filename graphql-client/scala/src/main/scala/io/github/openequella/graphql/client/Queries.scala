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

object Queries {

  /** Queries for admin console plugin
    */
  def adminConsolePlugin[A](
      innerSelection: SelectionBuilder[AdminConsolePluginQueries, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, A] =
    _root_.caliban.client.SelectionBuilder.Field("adminConsolePlugin", Obj(innerSelection))

  /** Queries for base entities
    */
  def baseEntities[A](
      innerSelection: SelectionBuilder[BaseEntityQueries, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, A] =
    _root_.caliban.client.SelectionBuilder.Field("baseEntities", Obj(innerSelection))

  /** Queries for Collections
    */
  def collection[A](
      innerSelection: SelectionBuilder[CollectionQueries, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, A] =
    _root_.caliban.client.SelectionBuilder.Field("collection", Obj(innerSelection))

  /** Queries for JavaScript
    */
  def javaScript[A](
      innerSelection: SelectionBuilder[JavaScriptQueries, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, A] =
    _root_.caliban.client.SelectionBuilder.Field("javaScript", Obj(innerSelection))

  /** Queries for language
    */
  def language[A](
      innerSelection: SelectionBuilder[LanguageQueries, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, A] =
    _root_.caliban.client.SelectionBuilder.Field("language", Obj(innerSelection))

  /** Queries for Metadata Schemas
    */
  def metadataSchema[A](
      innerSelection: SelectionBuilder[MetadataSchemaQueries, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, A] =
    _root_.caliban.client.SelectionBuilder.Field("metadataSchema", Obj(innerSelection))

  /** Queries for internal groups
    */
  def internalGroups[A](
      innerSelection: SelectionBuilder[InternalGroupQueries, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, A] =
    _root_.caliban.client.SelectionBuilder.Field("internalGroups", Obj(innerSelection))

  /** Queries for internal users
    */
  def internalUsers[A](
      innerSelection: SelectionBuilder[InternalUserQueries, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, A] =
    _root_.caliban.client.SelectionBuilder.Field("internalUsers", Obj(innerSelection))

  /** Queries for the user directory - users, groups, and roles from all configured user management
    * plugins
    */
  def userDirectory[A](
      innerSelection: SelectionBuilder[UserDirectoryQueries, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, A] =
    _root_.caliban.client.SelectionBuilder.Field("userDirectory", Obj(innerSelection))
}
