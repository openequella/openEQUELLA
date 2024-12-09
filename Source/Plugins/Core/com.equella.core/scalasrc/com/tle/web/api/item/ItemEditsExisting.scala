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

package com.tle.web.api.item

import java.util.Collections

import com.tle.beans.item.{ItemIdKey, ItemKey}
import com.tle.core.guice.Bind
import com.tle.legacy.LegacyGuice
import javax.inject.Singleton
import org.springframework.transaction.annotation.Transactional

@Bind
@Singleton
class ItemEditsExisting {

  @Transactional
  def performEdits(
      edits: ItemEdits,
      itemKey: ItemKey,
      lockId: String,
      ensureOnIndexList: Boolean
  ): com.tle.common.Pair[ItemEditResponses, ItemIdKey] = {
    val editor =
      LegacyGuice.itemEditorService.getItemEditor(itemKey, null, lockId, Collections.emptyList())
    val response  = ItemEdits.performEdits(edits, editor)
    val itemIdKey = editor.finishedEditing(ensureOnIndexList)
    new com.tle.common.Pair(response, itemIdKey)
  }
}
