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

package com.tle.core.usermanagement.standard.dao

import com.google.common.base.{CharMatcher, Splitter}
import com.tle.common.institution.CurrentInstitution
import org.hibernate.Session
import org.hibernate.criterion.Order
import org.hibernate.query.Query

import scala.jdk.CollectionConverters._

/**
  * A builder for creating a query for TLEUser which can be used for searching and/or counting (or
  * may other types of queries and projections).
  *
  * @tparam E The type of the entity to return from the query (commonly `TLEUser`, but could be numeric etc.)
  */
class UserQueryBuilder[E] {
  private var selectStatement: Option[String]      = None
  private var queryTokens: List[String]            = List.empty
  private var byGroupId: Option[(String, Boolean)] = None
  private var orderByFields: List[Order]           = List.empty

  /**
    * Set the SELECT statement for the query. e.g. specify `"count(*)"` to get a count of the results.
    * @param select a string to be added at the start of the query string following the `SELECT` keyword.
    * @return this UserQueryBuilder instance
    */
  def select(select: String): UserQueryBuilder[E] = {
    selectStatement = Option(select).filter(_.nonEmpty)
    this
  }

  /**
    * Set the query string to search for users by. This will be tokenised and used to search for users
    * where the first name, last name or username contains the token.
    *
    * @param query The query string to search for users by
    * @return this UserQueryBuilder instance
    */
  def withQueryString(query: String): UserQueryBuilder[E] = {
    queryTokens = Option(query).filter(_.nonEmpty).map(tokeniseQuery).getOrElse(List.empty)
    this
  }

  /**
    * Set the parent group ID to search for users by. If recurse is true, then users in the group and all
    * sub-groups will be returned.
    *
    * @param parentGroupID The ID of the parent group to search for users in
    * @param recurse Whether to search for users in sub-groups
    * @return this UserQueryBuilder instance
    */
  def withParentGroupID(parentGroupID: String, recurse: Boolean): UserQueryBuilder[E] = {
    byGroupId = Option(parentGroupID).filter(_.nonEmpty).map((_, recurse))
    this
  }

  /**
    * Add an ORDER BY clause to the query. Can be called multiple times for each field to order by.
    *
    * @param orderBy The Order to add to the query
    * @return this UserQueryBuilder instance
    */
  def orderBy(orderBy: Order): UserQueryBuilder[E] = {
    orderByFields = orderByFields :+ orderBy
    this
  }

  /**
    * Build the query using the current settings.
    *
    * @param session The Hibernate session to use to build the query
    * @return The built query
    */
  def build(session: Session): Query[E] = {
    val paramInstitution = "institution"
    val paramUserToken   = "token"
    val paramGroupId     = "groupID"

    // Step 1. Build the Query String
    val q = new StringBuilder(selectStatement.map("SELECT " + _ + " ").getOrElse(""))

    q.append(s"FROM TLEUser t WHERE t.institution = :$paramInstitution")

    val queryTokensWithIndex = queryTokens.zipWithIndex
    queryTokensWithIndex.foreach {
      case (_, i) =>
        val token = s":$paramUserToken$i"
        q.append(s" AND (LOWER(first_name) LIKE $token")
        q.append(s" OR LOWER(last_name) LIKE $token")
        q.append(s" OR LOWER(username) LIKE $token)")
    }

    byGroupId.foreach {
      case (_, recurse) =>
        q.append(" AND t.uuid IN (SELECT ELEMENTS(g.users) FROM TLEGroup g")
        if (recurse) {
          q.append(" LEFT OUTER JOIN g.allParents sg")
          q.append(
            s" WHERE g.institution = :$paramInstitution AND (sg.uuid = :$paramGroupId OR g.uuid = :$paramGroupId))")
        } else {
          q.append(s" WHERE g.institution = :$paramInstitution AND g.uuid = :$paramGroupId)")
        }
    }

    if (orderByFields.nonEmpty) {
      q.append(" ORDER BY ")
      q.append(
        orderByFields.map(_.toString).mkString(", ")
      )
    }

    // Step 2. Create the Query, and set the parameters
    val query: Query[E] = session.createQuery(q.toString).asInstanceOf[Query[E]]
    query.setParameter(paramInstitution, CurrentInstitution.get)
    queryTokensWithIndex.foreach {
      case (token, i) =>
        query.setParameter(s"$paramUserToken$i", token)
    }
    byGroupId.foreach {
      case (groupId, _) =>
        query.setParameter(paramGroupId, groupId)
    }

    query
  }

  /**
    * Tokenise the query string into a list of tokens, where:
    *
    * - each token is surrounded by %'s
    * - all tokens are lowercased
    * - all *'s are replaced with %'s
    *
    * @param query The query string to tokenise
    * @return A list of tokens
    */
  private def tokeniseQuery(query: String): List[String] = {
    // Prep the query by converting all *'s to %'s and lowercase it.
    val q = query.replace('*', '%').toLowerCase

    // Split it up on white space, removing leading/trailing % signs and
    // ignore empty strings.
    Splitter
      .onPattern("\\s")
      // In prep for below where we surround all tokens with %'s
      .trimResults(CharMatcher.is('%'))
      .omitEmptyStrings
      .split(q)
      .asScala
      .toList
      .map("%" + _ + "%")
  }
}
