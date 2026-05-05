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

package com.tle.web.api.item;

import com.tle.beans.item.ItemKey;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import java.net.URI;

public interface ItemLinkService {

  /**
   * Adds standard links to an item bean.
   *
   * @param itemBean The item bean to update.
   * @return The item bean with links added.
   */
  ItemBean addLinks(ItemBean itemBean);

  /**
   * Adds standard links to an Equella item bean.
   *
   * @param itemBean The item bean to update.
   * @return The item bean with links added.
   */
  EquellaItemBean addLinks(EquellaItemBean itemBean);

  /**
   * Gets the URI for an item.
   *
   * @param itemKey The item key.
   * @return The item URI.
   */
  URI getItemURI(ItemKey itemKey);
}
