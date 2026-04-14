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

import com.tle.beans.Language
import java.util
import java.lang

/** Service class for admin language operations via the GraphQL library.
  */
trait AdminLanguageService {

  /** Resolve display names for the provided language bundle IDs.
    *
    * @param bundleIds
    *   the language bundle IDs to resolve.
    * @return
    *   Map from bundle ID to localized display name.
    */
  def getNames(
      bundleIds: util.Collection[lang.Long]
  ): util.Map[lang.Long, String]

  /** List configured languages for the current institution.
    */
  def getLanguages: util.List[Language]
}
