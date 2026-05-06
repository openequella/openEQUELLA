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

package io.github.openequella.graphql.api

import caliban.client.Operations.RootQuery
import caliban.client.SelectionBuilder
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.client._

/** Represents an internal openEQUELLA group.
  *
  * @param uniqueId
  *   The unique identifier of the group.
  * @param parentId
  *   The unique identifier of the parent group, or None if this is a top-level group.
  * @param name
  *   The name of the group.
  * @param description
  *   The description for the group.
  * @param hasGroups
  *   Whether this group has sub-groups.
  * @param hasUsers
  *   Whether this group has users.
  */
final case class InternalGroupView(
    uniqueId: String,
    parentId: Option[String],
    name: String,
    description: Option[String],
    hasGroups: Boolean,
    hasUsers: Boolean
)

/** Provides access to the openEQUELLA internal group API.
  */
object InternalGroupApi extends NestedApi[InternalGroupQueries, InternalGroupMutations] {

  override protected def queryWrapper[A]
      : SelectionBuilder[InternalGroupQueries, A] => SelectionBuilder[RootQuery, A] =
    Queries.internalGroups

  override protected def mutationWrapper[A]
      : SelectionBuilder[InternalGroupMutations, A] => SelectionBuilder[
        _root_.caliban.client.Operations.RootMutation,
        A
      ] = Mutations.internalGroups

  private val tleGroup =
    (InternalGroup.uniqueId ~ InternalGroup.parentId ~ InternalGroup.name ~ InternalGroup.description ~ InternalGroup.hasGroups ~ InternalGroup.hasUsers)
      .mapN(InternalGroupView)
  private val groupEdge =
    (InternalGroupEdge.cursor ~ InternalGroupEdge.node { tleGroup })
      .mapN(NodeWithCursorView[InternalGroupView](_, _))
  private val groupConnection =
    (InternalGroupConnection.pageInfo { PageInfoView.selector } ~ InternalGroupConnection.edges {
      groupEdge
    })
      .mapN(ConnectionView[InternalGroupView](_, _))

  /** Retrieves the details of an individual group by its unique identifier.
    *
    * @param uniqueId
    *   The unique identifier of the group.
    * @param cfg
    *   The client configuration.
    * @return
    *   `Left` containing a list of errors or `Right` if the operation was successful. `Some` if
    *   group was found, `None` if not.
    */
  def getByUniqueId(uniqueId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[InternalGroupView]] = {
    val q = InternalGroupQueries.byId(uniqueId) {
      tleGroup
    }

    query(q)
  }

  /** Retrieves the details of an individual group by its name.
    *
    * @param name
    *   The name of the group.
    * @param cfg
    *   The client configuration.
    * @return
    *   `Left` containing a list of errors or `Right` if the operation was successful. `Some` if
    *   group was found, `None` if not.
    */
  def getByName(name: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[InternalGroupView]] = {
    val q = InternalGroupQueries.byName(name) {
      tleGroup
    }

    query(q)
  }

  /** Retrieves the details of multiple groups by their unique identifiers.
    *
    * @param pagination
    *   Detail the number of items to return, and whether to page through forward or backwards.
    *   Especially useful for paging through large result sets.
    * @param groupIds
    *   The unique identifiers of the groups to retrieve.
    * @param cfg
    *   The client configuration.
    * @return
    *   `Left` containing a list of errors or `Right` if the operation was successful with a list of
    *   groups as well as pagination information which can be used to get the next/previous page.
    */
  def getGroupsByIds(pagination: Pagination, groupIds: Set[String])(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[InternalGroupView]] =
    queryPaginated(pagination) { (first, last, before, after) =>
      InternalGroupQueries.listByIds(groupIds.toList, first, last, before, after) {
        groupConnection
      }
    }

  /** Creates a new group.
    *
    * @param name
    *   The name of the group.
    * @param parentId
    *   The unique identifier of the parent group, or None if this is a top-level group.
    * @param cfg
    *   The client configuration.
    * @return
    *   `Left` containing a list of errors or `Right` if the operation was successful.
    */
  def createGroup(name: String, parentId: Option[String] = None)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], InternalGroupView] =
    flatMutate(InternalGroupMutations.create(name, parentId) { tleGroup })

  /** Deletes a group and all its child subgroups and all contained users.
    *
    * @param uniqueId
    *   The unique identifier of the group.
    * @param cfg
    *   The client configuration.
    * @return
    *   `Left` containing a list of errors or `Right` if the operation was successful.
    * @see
    *   [[deleteGroupOnly]] to delete the group while preserving its children.
    */
  def deleteGroup(uniqueId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Unit] =
    flatMutate(InternalGroupMutations.delete(uniqueId, deleteChildren = true))

  /** Deletes a group without deleting its children.
    *
    * Child subgroups are reparented to the deleted group's parent rather than being removed.
    *
    * @param uniqueId
    *   The unique identifier of the group.
    * @param cfg
    *   The client configuration.
    * @return
    *   `Left` containing a list of errors or `Right` if the operation was successful.
    * @see
    *   [[deleteGroup]] to delete the group and all its descendants.
    */
  def deleteGroupOnly(uniqueId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Unit] =
    flatMutate(InternalGroupMutations.delete(uniqueId, deleteChildren = false))

  /** Updates a group, and the primary means to add users to a group.
    *
    * @param uniqueId
    *   The unique identifier of the group.
    * @param name
    *   The new name of the group, or `None` if the name should not be changed.
    * @param description
    *   The new description of the group, or `None` if the description should not be changed. To
    *   clear the description, pass an empty string - e.g. `Some("")`.
    * @param parentId
    *   The new parent group, or `None` if the parent should not be changed.
    * @param users
    *   The new list of users in the group, or `None` if the users should not be changed - and empty
    *   `List` if you want to remove all users. The specified users are 'remote' users, and so can
    *   be internal or external (LDAP, LTI, etc.) users.
    * @param cfg
    *   The client configuration.
    * @return
    *   `Left` containing a list of errors or `Right` if the operation was successful.
    */
  def updateGroup(
      uniqueId: String,
      name: Option[String] = None,
      description: Option[String] = None,
      parentId: Option[String] = None,
      users: Option[List[String]] = None
  )(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], InternalGroupView] =
    flatMutate(
      InternalGroupMutations.update(uniqueId, name, description, parentId, users) { tleGroup }
    )

  /** Lists a single level of groups. To retrieve the whole tree of groups, you will need to call
    * this method multiple times, once for each level of the hierarchy. (That is, use tree walking.)
    *
    * @param pagination
    *   Detail the number of items to return, and whether to page through forward or backwards.
    *   Especially useful for paging through large result sets.
    * @param parentId
    *   The unique identifier of the parent group, or None to list top-level groups.
    * @param cfg
    *   The client configuration.
    * @return
    *   `Left` containing a list of errors or `Right` if the operation was successful with a list of
    *   groups as well as pagination information which can be used to get the next/previous page.
    */
  def listGroups(pagination: Pagination, parentId: Option[String] = None)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[InternalGroupView]] =
    queryPaginated(pagination) { (first, last, before, after) =>
      InternalGroupQueries.list(parentId, first, last, before, after) {
        groupConnection
      }
    }

  /** Lists the users in a group.
    *
    * @param pagination
    *   Detail the number of items to return, and whether to page through forward or backwards.
    *   Especially useful for paging through large result sets.
    * @param groupId
    *   The unique identifier of the group for which to get users for.
    * @param cfg
    *   The client configuration.
    * @return
    *   `Left` containing a list of errors or `Right` if the operation was successful with a list of
    *   users as well as pagination information which can be used to get the next/previous page.
    */
  def listGroupUsers(pagination: Pagination, groupId: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[String]] = {
    val userEdge =
      (StringEdge.cursor ~ StringEdge.node).mapN(NodeWithCursorView[String](_, _))
    val userConnection =
      (StringConnection.pageInfo { PageInfoView.selector } ~ StringConnection.edges { userEdge })
        .mapN(ConnectionView[String](_, _))

    queryPaginated(pagination) { (first, last, before, after) =>
      InternalGroupQueries.users(groupId, first, last, before, after) {
        userConnection
      }
    }
  }

  /** Searches for groups based on the provided query. Will return a list of groups without regard
    * for hierarchy. Therefore, if the wish is to show the search results in a tree structure, then
    * the client will need to do this with calls to `listGroups`.
    *
    * @param pagination
    *   Detail the number of items to return, and whether to page through forward or backwards.
    *   Especially useful for paging through large result sets.
    * @param query
    *   A term (or partial term) to search all groups by.
    * @param cfg
    *   The client configuration.
    * @return
    *   `Left` containing a list of errors or `Right` if the operation was successful with a list of
    *   groups as well as pagination information which can be used to get the next/previous page.
    */
  def searchGroups(pagination: Pagination, query: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], PaginationResult[InternalGroupView]] =
    queryPaginated(pagination) { (first, last, before, after) =>
      InternalGroupQueries.search(query, first, last, before, after) {
        groupConnection
      }
    }
}
