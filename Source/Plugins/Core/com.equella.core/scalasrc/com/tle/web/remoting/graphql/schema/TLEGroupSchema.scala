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

package com.tle.web.remoting.graphql.schema

import caliban._
import caliban.relay.{Base64Cursor, Pagination, PaginationArgs}
import caliban.schema.Annotations.GQLDescription
import caliban.schema.ArgBuilder.auto._
import caliban.schema.Schema.auto._
import com.tle.core.guice.Bind
import com.tle.web.remoting.graphql.provider.TLEGroupProvider
import zio.IO

import javax.inject.{Inject, Singleton}

/** The schema for the TLE Group GraphQL API. Because `TLEGroup`s are effectively 'internal' groups,
  * all operations in this schema using the namespace prefix of `internalGroup`.
  */
@Bind
@Singleton
class TLEGroupSchema extends SchemaProvider {
  private var tleGroupProvider: TLEGroupProvider = _

  /** Default constructor for Guice.
    */
  @Inject def this(tleGroupProvider: TLEGroupProvider) = {
    this()
    this.tleGroupProvider = tleGroupProvider
  }

  override def getApi: GraphQL[Any] = graphQL(
    RootResolver(
      queries,
      mutations
    )
  )

  private val queries = Queries(
    internalGroupById = args => tleGroupProvider.groupById(args.uniqueId),
    internalGroupByName = args => tleGroupProvider.groupByName(args.name),
    internalGroups = args =>
      for {
        pagination <- Pagination(args)
        groups     <- tleGroupProvider.listGroups(args.parentId, pagination)
      } yield groups,
    internalGroupSearch = args => tleGroupProvider.searchGroups(args.query),
    internalGroupUsers = args => tleGroupProvider.listGroupUsers(args.uniqueId)
  )

  private val mutations = Mutations(
    internalGroupCreate = args => tleGroupProvider.createGroup(args.name, args.parentId),
    internalGroupDelete = args => tleGroupProvider.deleteGroup(args.uniqueId, args.deleteChildren),
    internalGroupUpdate =
      args => tleGroupProvider.updateGroup(args.uniqueId, args.name, args.parentId, args.users)
  )

  case class Queries(
      @GQLDescription("Retrieve a group by its unique ID")
      internalGroupById: GroupByIdArgs => Option[Group],
      @GQLDescription("Retrieve a group by its name")
      internalGroupByName: GroupByNameArgs => Option[Group],
      @GQLDescription(
        "List all groups at a specific level in the hierarchy determined by the parent ID - or none for the root."
      )
      internalGroups: ListGroupsArgs => IO[CalibanError, GroupConnection],
      // TODO: Add pagination
      @GQLDescription("Search for groups anywhere within the hierarchy by name (wildcard search)")
      internalGroupSearch: GroupSearchArgs => List[Group],
      @GQLDescription("List user ids for all users in the specified group")
      internalGroupUsers: ListGroupUsersArgs => List[String]
  )

  case class GroupByIdArgs(
      @GQLDescription("The unique ID of the group to retrieve")
      uniqueId: String
  )

  case class GroupByNameArgs(
      @GQLDescription("The name of the group to retrieve")
      name: String
  )

  case class GroupSearchArgs(
      @GQLDescription(
        "The query string to search for groups by (server will surround with wildcard)"
      )
      query: String
  )

  case class ListGroupsArgs(
      @GQLDescription("The unique ID of the parent group to list groups for - or none for the root")
      parentId: Option[String],
      @GQLDescription(
        "Pagination - how many items to return from the start of the possible list of items"
      )
      first: Option[Int],
      @GQLDescription(
        "Pagination - how many items to return from the end of the possible list of items"
      )
      last: Option[Int],
      @GQLDescription(
        "Pagination - the cursor for a item before which all items should be returned"
      )
      before: Option[String],
      @GQLDescription("Pagination - the cursor for a item after which all items should be returned")
      after: Option[String]
  ) extends PaginationArgs[Base64Cursor]

  case class ListGroupUsersArgs(
      @GQLDescription("The unique ID of the group to list users for")
      uniqueId: String
  )

  case class Mutations(
      @GQLDescription("Create a new group")
      internalGroupCreate: GroupCreateArgs => ResultWithErrors[Group],
      @GQLDescription("Delete a group by its unique ID")
      internalGroupDelete: GroupDeleteArgs => ResultWithErrors[Unit],
      @GQLDescription(
        "Update a group by its unique ID - can also be used to move group within the hierarchy by changing the parent ID"
      )
      internalGroupUpdate: GroupUpdateArgs => ResultWithErrors[Group]
  )

  case class GroupCreateArgs(
      @GQLDescription("The name of the group to create")
      name: String,
      @GQLDescription(
        "The unique ID of the parent group to create the new group under - or none for the root"
      )
      parentId: Option[String]
  )

  case class GroupDeleteArgs(
      @GQLDescription("The unique ID of the group to delete")
      uniqueId: String,
      @GQLDescription(
        "Whether to delete all children of the group, or move them to the parent of the group being deleted"
      )
      deleteChildren: Boolean
  )

  case class GroupUpdateArgs(
      @GQLDescription("The unique ID of the group to update")
      uniqueId: String,
      @GQLDescription("The new name of the group")
      name: Option[String],
      @GQLDescription("The new parent ID of the group")
      parentId: Option[String],
      @GQLDescription(
        "The updated list of users in the group - replacing what was previously there."
      )
      users: Option[Set[String]]
  )
}
