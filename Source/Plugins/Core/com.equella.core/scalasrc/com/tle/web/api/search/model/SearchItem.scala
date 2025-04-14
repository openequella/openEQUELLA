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

package com.tle.web.api.search.model

import com.tle.beans.item.ItemIdKey
import com.tle.core.item.serializer.ItemSerializerItemBean
import com.tle.core.services.item.FreetextResult
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean

/** This class provides general information of an Item to be used inside a SearchResult.
  *
  * @param idKey
  *   An ItemIdKey
  * @param bean
  *   An EquellaItemBean
  * @param keywordFound
  *   Indicates if a search term has been found inside attachment content
  */
case class SearchItem(idKey: ItemIdKey, bean: EquellaItemBean, keywordFound: Boolean)
object SearchItem {
  def apply(
      itemIdKey: ItemIdKey,
      isKeywordFoundInAttachment: Boolean,
      serializer: ItemSerializerItemBean
  ): SearchItem = {
    val itemBean = new EquellaItemBean
    itemBean.setUuid(itemIdKey.getUuid)
    itemBean.setVersion(itemIdKey.getVersion)
    serializer.writeItemBeanResult(itemBean, itemIdKey.getKey)
    LegacyGuice.itemLinkService.addLinks(itemBean)
    SearchItem(itemIdKey, itemBean, isKeywordFoundInAttachment)
  }

  def apply(item: FreetextResult, serializer: ItemSerializerItemBean): SearchItem = {
    val keywordFoundInAttachment = item.isKeywordFoundInAttachment
    val itemId                   = item.getItemIdKey
    SearchItem(itemId, keywordFoundInAttachment, serializer)
  }
}
