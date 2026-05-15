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

package com.tle.common.util

import io.lemonlabs.uri.Url
import com.dytech.edge.web.WebConstants.{HTTP, HTTPS}

object UrlUtils {

  /** Safely checks if a given string is a valid, absolute HTTP or HTTPS URL.
    *
    * To return true, the URL string must:
    *   1. Be successfully parsed without errors.
    *   2. Have a scheme of either "http" or "https".
    *   3. Contain a defined host (e.g., "example.com").
    *
    * @param url
    *   The URL string to validate.
    * @return
    *   True if the string meets all absolute HTTP/HTTPS criteria, false otherwise.
    */
  def isAbsoluteHttpUrl(url: String): Boolean =
    Url
      .parseTry(url)
      .toOption
      .exists(u =>
        u.schemeOption.exists(Set(HTTP, HTTPS)) &&
          u.hostOption.isDefined
      )
}
