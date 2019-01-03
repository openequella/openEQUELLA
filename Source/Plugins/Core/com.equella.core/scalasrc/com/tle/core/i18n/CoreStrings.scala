/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.core.i18n

import com.tle.common.i18n.StringLookup
import com.tle.web.resources.ResourcesService

object CoreStrings extends StringLookup {

  val lookup = ResourcesService.getResourceHelper("com.equella.core")

  def key(local: String): String = lookup.key(local)

  def text(key: String) : String = text(key, Seq.empty: _*)

  override def text(key: String, vals: AnyRef*): String = lookup.getString(key, vals: _*)

  override def prefix(prefix: String): StringLookup = StringLookup.prefixed(key(prefix))
}
