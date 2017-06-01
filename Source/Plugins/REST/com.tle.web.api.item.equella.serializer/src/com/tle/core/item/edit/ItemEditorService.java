/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.item.edit;

import java.util.List;

import com.tle.beans.item.ItemKey;
import com.tle.core.item.serializer.ItemDeserializerEditor;

public interface ItemEditorService
{
	ItemEditor getItemEditor(ItemKey itemKey, String stagingUuid, String lockId,
		List<ItemDeserializerEditor> deserializerEditors);

	ItemEditor newItemEditor(String collectionUuid, ItemKey itemKey, String stagingUuid,
		List<ItemDeserializerEditor> deserializerEditors);

	ItemEditor importItemEditor(String collectionUuid, ItemKey itemKey, String stagingUuid,
		List<ItemDeserializerEditor> deserializerEditors);
}
