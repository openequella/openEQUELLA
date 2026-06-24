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

package io.github.openequella.graphql.client

import caliban.client._
import caliban.client.__Value._

final case class CollectionWizardInput(
    name: scala.Option[String] = None,
    redraftScript: scala.Option[String] = None,
    saveScript: scala.Option[String] = None,
    allowNonSequentialNavigation: Boolean,
    showPageTitlesNextPrev: Boolean,
    additionalCssClass: scala.Option[String] = None,
    pages: scala.Option[String] = None,
    fixedMetadata: scala.Option[String] = None
)
object CollectionWizardInput {
  implicit val encoder: ArgEncoder[CollectionWizardInput] = new ArgEncoder[CollectionWizardInput] {
    override def encode(value: CollectionWizardInput): __Value =
      __ObjectValue(
        List(
          "name" -> value.name.fold(__NullValue: __Value)(value =>
            implicitly[ArgEncoder[String]].encode(value)
          ),
          "redraftScript" -> value.redraftScript.fold(__NullValue: __Value)(value =>
            implicitly[ArgEncoder[String]].encode(value)
          ),
          "saveScript" -> value.saveScript.fold(__NullValue: __Value)(value =>
            implicitly[ArgEncoder[String]].encode(value)
          ),
          "allowNonSequentialNavigation" -> implicitly[ArgEncoder[Boolean]]
            .encode(value.allowNonSequentialNavigation),
          "showPageTitlesNextPrev" -> implicitly[ArgEncoder[Boolean]]
            .encode(value.showPageTitlesNextPrev),
          "additionalCssClass" -> value.additionalCssClass.fold(__NullValue: __Value)(value =>
            implicitly[ArgEncoder[String]].encode(value)
          ),
          "pages" -> value.pages.fold(__NullValue: __Value)(value =>
            implicitly[ArgEncoder[String]].encode(value)
          ),
          "fixedMetadata" -> value.fixedMetadata.fold(__NullValue: __Value)(value =>
            implicitly[ArgEncoder[String]].encode(value)
          )
        )
      )
  }
}
