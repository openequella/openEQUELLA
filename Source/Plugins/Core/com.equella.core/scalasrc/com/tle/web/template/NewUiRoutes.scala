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

package com.tle.web.template

import io.lemonlabs.uri.{QueryString, RelativeUrl}

/**
  * Centralises the various routes for the New UI - especially useful when doing `forwardToUrl`.
  */
object NewUiRoutes {
  val PATH_MYRESOURCES: String = "page/myresources"

  private def buildRelativeUrl(path: String, params: Vector[(String, Option[String])]): String =
    RelativeUrl.parse(path).withQueryString(QueryString(params)).toString()

  def myResources(myResourcesType: String,
                  subStatus: Option[String] = None,
                  stateId: Option[String] = None): String = {
    val params =
      Vector(("type", Some(myResourcesType)), ("mstatus", subStatus), ("newUIStateId", stateId))
        .filter({
          case (_, v) => v.isDefined
        })
    buildRelativeUrl(path = PATH_MYRESOURCES, params)
  }
}
