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

package com.tle.admin.graphql.conversion

import com.tle.beans.entity.schema.Citation
import io.github.openequella.graphql.api.views.CitationView
import scala.util.chaining.scalaUtilChainingOps

object CitationViewConverter {
  def toCitation(view: CitationView): Citation = new Citation().tap { c =>
    c.setName(view.name)
    c.setTransformation(view.transformation)
  }

  def fromCitation(citation: Citation): CitationView = {
    val name = Option(citation.getName).getOrElse(
      throw new IllegalArgumentException("Citation name must not be null")
    )
    val transformation = Option(citation.getTransformation).getOrElse(
      throw new IllegalArgumentException("Citation transformation must not be null")
    )

    CitationView(name = name, transformation = transformation)
  }
}
