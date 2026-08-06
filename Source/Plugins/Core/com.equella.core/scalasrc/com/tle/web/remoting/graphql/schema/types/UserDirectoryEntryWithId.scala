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

/** An entry in the result of fetching multiple users by IDs.
  */
@GQLDescription(
  "An entry in the result of fetching multiple users by IDs."
)
case class UserWithId(
    @GQLDescription("The requested user ID") id: String,
    @GQLDescription("The resolved user") user: User
)

/** An entry in the result of fetching multiple groups by IDs.
  */
@GQLDescription(
  "An entry in the result of fetching multiple groups by IDs."
)
case class GroupWithId(
    @GQLDescription("The requested group ID") id: String,
    @GQLDescription("The resolved group") group: Group
)

/** An entry in the result of fetching multiple roles by IDs.
  */
@GQLDescription(
  "An entry in the result of fetching multiple roles by IDs."
)
case class RoleWithId(
    @GQLDescription("The requested role ID") id: String,
    @GQLDescription("The resolved role") role: Role
)
