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

import caliban.client.FieldBuilder._
import caliban.client._

object LanguageString {

  /** Unique identifier for this language string.
    */
  def id: SelectionBuilder[LanguageString, Long] =
    _root_.caliban.client.SelectionBuilder.Field("id", Scalar())

  /** Priority of this language string, with lower numbers indicating higher priority.
    */
  def priority: SelectionBuilder[LanguageString, Int] =
    _root_.caliban.client.SelectionBuilder.Field("priority", Scalar())

  /** Locale code for this language string, e.g., 'en' for English.
    */
  def locale: SelectionBuilder[LanguageString, String] =
    _root_.caliban.client.SelectionBuilder.Field("locale", Scalar())

  /** The translated text for this locale.
    */
  def text: SelectionBuilder[LanguageString, String] =
    _root_.caliban.client.SelectionBuilder.Field("text", Scalar())
}
