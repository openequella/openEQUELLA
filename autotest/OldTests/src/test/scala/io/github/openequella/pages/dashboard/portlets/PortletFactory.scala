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

package io.github.openequella.pages.dashboard.portlets

import com.tle.webtests.framework.PageContext
import io.github.openequella.pages.dashboard.PortletType

/** Factories for different portlet types.
  */
sealed trait PortletFactory[P <: GenericPortlet[_]] {

  /** The type of portlet this factory creates.
    */
  def portletType: PortletType.Value

  /** Function to create the portlet instance.
    */
  def create(ctx: PageContext, title: String): P
}

object PortletFactory {
  case object Generic extends PortletFactory[GenericPortlet[_]] {
    val portletType: PortletType.Value          = PortletType.Favourites
    def create(ctx: PageContext, title: String) = new GenericPortlet(ctx, title)
  }

  case object Favourites extends PortletFactory[FavouritesPortlet] {
    val portletType: PortletType.Value          = PortletType.Favourites
    def create(ctx: PageContext, title: String) = new FavouritesPortlet(ctx, title)
  }
}
