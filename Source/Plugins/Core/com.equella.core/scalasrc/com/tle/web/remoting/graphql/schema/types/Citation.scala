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

package com.tle.web.remoting.graphql.schema.types

import caliban.schema.Annotations.GQLDescription

/** GraphQL representation of `com.tle.beans.entity.schema.Citation`.
  *
  * @see
  *   [[com.tle.beans.entity.schema.Citation]]
  */
@GQLDescription(
  "A configuration of a transform which controls how licensed materials are cited in openEQUELLA."
)
final case class Citation(
    @GQLDescription("Name for the citation")
    name: String,
    @GQLDescription("Name of the XSLT file for citation - can be downloaded from the server")
    transformation: String
)
object Citation {
  def apply(citation: com.tle.beans.entity.schema.Citation): Citation = {
    Citation(
      name = citation.getName,
      transformation = citation.getTransformation
    )
  }
}
