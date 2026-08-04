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

package object client {
  type AdminConsolePluginQueries
  type BaseEntityQueries
  type BaseEntityReference
  type BaseEntitySecurity
  type Citation
  type CollectionDefinition
  type CollectionDisplayNode
  type CollectionDynamicMetadataRule
  type CollectionHtmlMapping
  type CollectionImsMapping
  type CollectionItemMetadataRule
  type CollectionLiteral
  type CollectionLiteralMapping
  type CollectionMetadataMapping
  type CollectionMutations
  type CollectionQueries
  type CollectionSearchDetails
  type CollectionSummaryDisplayTemplate
  type CollectionSummarySectionConfig
  type CollectionWizard
  type EditableEntityCollectionDefinition
  type EditableEntityMetadataSchema
  type EditableEntitySkeleton
  type EntityDetails
  type Group
  type GroupConnection
  type GroupEdge
  type GroupWithId
  type InternalGroup
  type InternalGroupConnection
  type InternalGroupEdge
  type InternalGroupMutations
  type InternalGroupQueries
  type InternalUserMutations
  type InternalUserQueries
  type JavaScriptQueries
  type KVStringString
  type Language
  type LanguageBundle
  type LanguageBundleName
  type LanguageQueries
  type LanguageString
  type MetadataSchema
  type MetadataSchemaMutations
  type MetadataSchemaQueries
  type MetadataSchemaTransform
  type NameValue
  type OtherTargetList
  type PageInfo
  type PluginDetails
  type Role
  type RoleConnection
  type RoleEdge
  type RoleWithId
  type StringConnection
  type StringEdge
  type TargetListEntry
  type User
  type UserConnection
  type UserDirectoryConfigMutations
  type UserDirectoryConfigQueries
  type UserDirectoryQueries
  type UserEdge
  type UserWithId
  type Queries   = _root_.caliban.client.Operations.RootQuery
  type Mutations = _root_.caliban.client.Operations.RootMutation
}
