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

import com.tle.admin.graphql.conversion.CollectionLiteralViewConverter.toLiteral
import com.tle.beans.entity.itemdef.mapping.LiteralMapping
import io.github.openequella.graphql.api.views.CollectionLiteralMappingView

import scala.util.chaining.scalaUtilChainingOps

object CollectionLiteralMappingViewConverter {
  def toLiteralMapping(view: CollectionLiteralMappingView): LiteralMapping =
    new LiteralMapping().tap { m =>
      m.setValue(view.value.orNull)
      // LiteralMapping.getLiterals() returns a mutable Collection, so we can addAll
      m.getLiterals.addAll(view.literals.map(toLiteral).asArrayList)
    }
}
