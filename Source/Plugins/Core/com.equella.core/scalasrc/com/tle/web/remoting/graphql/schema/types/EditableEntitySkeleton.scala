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

package com.tle.web.remoting.graphql.schema.types

import caliban.schema.Annotations.GQLDescription

import java.util.UUID

/** This provides the bare minimum needed for a client to create a new editable entity. With this
  * skeleton, the client can capture the initial required values in an EditableEntity and send it
  * back for a stopEdit related mutation.
  */
@GQLDescription(
  "A skeleton for creating a new editable entity, containing essential metadata such as UUID, owner, and staging ID."
)
final case class EditableEntitySkeleton(
    @GQLDescription("Unique identifier for the new entity")
    uuid: String,
    @GQLDescription("Owner of the new entity, typically the user who is creating it")
    owner: String,
    @GQLDescription("Staging ID for the new entity, used to store files before they are committed")
    stagingId: String
)
object EditableEntitySkeleton {
  def apply(
      owner: String,
      stagingId: String
  ): EditableEntitySkeleton = EditableEntitySkeleton(
    uuid = UUID.randomUUID().toString,
    owner = owner,
    stagingId = stagingId
  )
}
