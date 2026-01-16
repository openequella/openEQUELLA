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

package com.tle.admin.service

import com.tle.beans.user.GroupTreeNode
import io.github.openequella.graphql.api.TleGroupView
import org.slf4j.{Logger, LoggerFactory}

import scala.annotation.tailrec
import scala.collection.mutable

/** This class is responsible for building a tree structure of groups based on the provided
  * functions to retrieve groups. It allows for searching and filtering groups based on a query,
  * while also maintaining the hierarchical relationships between groups and their subgroups.
  *
  * @param getGroupsByQuery
  *   A function that retrieves a list of `TleGroupView` objects based on a search query.
  * @param getListGroups
  *   A function that retrieves a list of `TleGroupView` objects based on an optional parent ID.
  * @param getGroup
  *   A function that retrieves a single `TleGroupView` object based on its unique ID.
  */
class GroupTreeBuilder(
    getGroupsByQuery: String => List[TleGroupView],
    getListGroups: Option[String] => List[TleGroupView],
    getGroup: String => Option[TleGroupView]
) {
  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[GroupTreeBuilder])

  // This cache is used to store the nodes of the tree as they are created. It ensures that each
  // group is represented by a single node in the tree, even if it appears in multiple places in
  // the hierarchy.
  private val nodeCache = mutable.HashMap[String, GroupTreeNode]()

  /** Builds a tree of groups based on the provided search query. If the query is empty, it builds
    * the entire tree of groups. Otherwise, it searches for groups matching the query and builds a
    * tree for each found group.
    *
    * @param query
    *   The search query to filter groups. If empty, all groups are retrieved.
    * @return
    *   A `GroupTreeNode` representing the root of the group tree, with all groups and their
    *   subgroups.
    */
  def buildSearchTree(query: String): GroupTreeNode = {
    val groupTree = new GroupTreeNode()
    query match {
      case query if query.isEmpty =>
        // If there is no search query, we simply build the whole tree
        LOGGER.debug("Retrieving all groups")
        buildGroupTree(List((groupTree, None)))
      case _ =>
        // If there is a search query, we want to search for groups. Then we build the full tree
        // for each group found. And then we add it to the tree.
        LOGGER.debug("Searching for groups: [{}]", query)
        val groups = getGroupsByQuery(query)
        groups.foreach { group =>
          LOGGER.debug("Adding group to tree: [{}]", group.name)
          val node = newNode(group)
          // First build the tree for that group down
          buildGroupTree(List((node, Some(group.uniqueId))))
          // Then build the tree for that group up
          groupTree.add(buildParentTree(node, group.parentId))
        }
    }
    groupTree
  }

  /** Recursively builds the group tree by traversing the hierarchy of groups. It starts with the
    * given nodes and iteratively adds child groups to their respective parent nodes. The process
    * continues until all nodes have been processed.
    *
    * It is important to note that this method is designed to be tail-recursive, which means it can
    * handle large trees without causing a stack overflow. The recursion is optimized to avoid deep
    * recursion by using a loop-like structure.
    *
    * Specifically, it maintains a list of nodes to be processed, where each node is a tuple
    * containing the parent node and its corresponding parent ID. The method processes each node in
    * the list, adding child groups to the parent node and then recursively calling itself with the
    * updated list of nodes.
    *
    * @param nodes
    *   a list of tuples containing the parent node and its corresponding parent ID
    */
  @tailrec
  private def buildGroupTree(nodes: List[(GroupTreeNode, Option[String])]): Unit = nodes match {
    case Nil =>
      // All done
      ()
    case (parentNode, parentId) :: remainingNodes =>
      // Pop off the first node and process that, adding any further child nodes to the list for
      // processing
      val childGroups = getListGroups(parentId)
      val moreNodes   = processChildNodes(parentNode, childGroups)

      buildGroupTree(remainingNodes ::: moreNodes)
  }

  /** Processes the child groups of a given parent node and adds them to the tree. It creates a new
    * `GroupTreeNode` for each child group and adds it to the parent node. If a child group has its
    * own subgroups, it is added to the list of nodes to be processed further.
    *
    * @param parentNode
    *   The parent node to which the child groups will be added.
    * @param childGroups
    *   A list of `TleGroupView` objects representing the child groups.
    * @return
    *   A list of tuples containing representing groups which have subgroups and thereby need to be
    *   processed further.
    */
  private def processChildNodes(
      parentNode: GroupTreeNode,
      childGroups: List[TleGroupView]
  ): List[(GroupTreeNode, Option[String])] =
    childGroups.foldLeft(List[(GroupTreeNode, Option[String])]()) { (nodesWithChildren, g) =>
      val node = newNode(g)
      parentNode.add(node)

      if (g.hasGroups) nodesWithChildren :+ (node, Some(g.uniqueId)) else nodesWithChildren
    }

  @tailrec
  private def buildParentTree(
      childNode: GroupTreeNode,
      parentId: Option[String]
  ): GroupTreeNode =
    parentId match {
      case Some(id) =>
        getGroup(id) match {
          case None        => throw new IllegalStateException("Parent ID could not be found")
          case Some(group) =>
            val parentNode = newNode(group)
            parentNode.add(childNode)
            buildParentTree(parentNode, group.parentId)
        }
      case None =>
        // If there is no parent ID, we have reached the top of the tree
        childNode
    }

  /** Creates or retrieves a `GroupTreeNode` for the given `TleGroupView`.
    *
    * This function ensures that each group in the tree is represented by a single `GroupTreeNode`
    * instance. It uses a cache (`nodeCache`) to store and retrieve nodes by their unique ID. This
    * caching mechanism is essential to avoid creating duplicate nodes for the same group, which
    * ensures that the tree structure remains consistent and properly merged when building parent
    * and child relationships.
    *
    * @param group
    *   The `TleGroupView` representing the group for which a node is created or retrieved.
    * @return
    *   The `GroupTreeNode` corresponding to the given group.
    */
  private def newNode(group: TleGroupView): GroupTreeNode = {
    val id = group.uniqueId
    nodeCache.getOrElseUpdate(id, new GroupTreeNode(id, group.name))
  }
}
