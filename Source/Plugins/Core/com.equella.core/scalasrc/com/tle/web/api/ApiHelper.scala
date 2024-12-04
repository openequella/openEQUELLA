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

package com.tle.web.api

import cats.data.{NonEmptyChain, Validated}
import com.tle.beans.entity.BaseEntity
import com.tle.core.i18n.TextBundle
import com.tle.legacy.LegacyGuice
import javax.ws.rs.core.Response.{ResponseBuilder, Status}
import javax.ws.rs.core.{Response, UriBuilder}

object ApiHelper {
  private val bundleCache = LegacyGuice.bundleCache

  def entityOrNotFound[A](o: Option[A]): ResponseBuilder =
    o.fold(Response.status(Status.NOT_FOUND))(Response.ok(_))

  def validationOrOk[A](validated: Validated[NonEmptyChain[A], Boolean]): ResponseBuilder =
    validationOr(validated.map(ok => if (ok) Response.ok() else Response.status(Status.NOT_FOUND)))

  def validationOr[A](validated: Validated[NonEmptyChain[A], ResponseBuilder]): ResponseBuilder =
    validated.valueOr(errs =>
      Response.status(Response.Status.BAD_REQUEST).entity(errs.toNonEmptyVector.toVector)
    )

  def validationOrEntity[A, B](validated: Validated[NonEmptyChain[A], B]): ResponseBuilder =
    validationOr(validated.map(Response.ok))

  def apiUriBuilder(): UriBuilder = UriBuilder.fromPath("api/")

  def getEntityName(be: BaseEntity): String =
    TextBundle.getLocalString(be.getName, bundleCache, null, be.getUuid)
}
