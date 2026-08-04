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

import com.tle.common.NameValue

/** Service class for admin operations on JavaScript libraries and modules via the GraphQL library.
  */
trait AdminJavaScriptService {

  /** Get all JavaScript library names and IDs.
    */
  def listLibraries: java.util.List[NameValue]

  /** Get all JavaScript module names and IDs for the specified library.
    *
    * @param libraryId
    *   the identity of the JavaScript library.
    * @return
    *   None if the library does not exist, otherwise return a list of name and ID pairs.
    */
  def modulesByLibraryId(libraryId: String): java.util.Optional[java.util.List[NameValue]]
}
