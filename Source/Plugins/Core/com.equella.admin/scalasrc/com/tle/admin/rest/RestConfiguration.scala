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

package com.tle.admin.rest

import sttp.model.Uri

/** Configuration for the REST API client. Pretty well identical to the one we get from the GraphQL
  * Client Library - however, that one is technically outside the control of this codebase. So it
  * makes sense to have a similar one here where full control is maintained. It does unfortunately
  * result in some code duplication.
  *
  * @param institutionUrl
  *   The URL of the institution to connect to.
  */
final case class RestConfiguration(
    institutionUrl: Uri
) {
  def apiUrl(): Uri = {
    institutionUrl.addPath("api")
  }
}
