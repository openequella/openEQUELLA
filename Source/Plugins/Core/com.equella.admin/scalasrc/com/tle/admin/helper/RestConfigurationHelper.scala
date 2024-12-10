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

package com.tle.admin.helper

import com.tle.admin.rest.RestConfiguration
import sttp.model.Uri

import java.net.URL

/** Helper class for Java interop with the RestConfiguration class. Main area of helping is loading
  * system cookies. As can be seen in the login methods (e.g.
  * `com.tle.admin.boot.Bootstrap#login(java.net.URL)`) the admin console uses the
  * `CookieHandler.getDefault` method to load system cookies. This helper class provides a way to
  * load system cookies into a RestConfiguration object.
  *
  * The main cookie of interest is the `JSESSIONID` cookie. This cookie is used to maintain a
  * session with the openEQUELLA.
  *
  * Note that this is largely a duplicate of `ClientConfigurationHelper` in the same package.
  * Alternatively, this duplication could've been abstracted out with typeclasses, however, there is
  * only ever intended to be two of these. So the decision was to keep it simple.
  */
object RestConfigurationHelper {

  /** Create a RestConfiguration object from a java URL.
    */
  def create(url: URL): RestConfiguration = {
    RestConfiguration(Uri(url.toURI))
  }

  /** Load system cookies into a RestConfiguration object to match those which have been used after
    * logging into openEQUELLA.
    */
  def loadSystemCookies(cfg: RestConfiguration): Unit = {
    cfg.cookies.addAll(CookieHelper.getSystemCookies(cfg.institutionUrl))
  }

}
