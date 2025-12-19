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

package io.github.openequella.pages.dashboard

/** Enumeration of the different portlet types available. The value represents the text of the
  * default portlet title in the UI.
  */
object PortletType extends Enumeration {
  type PortletType = Value

  /** Only for testing purposes, represents a generic portlet when the type is unknown.
    */
  val Generic: Value = Value("Generic")

  val Browse: Value     = Value("Browse")
  val Favourites: Value = Value("Favourites")
}
