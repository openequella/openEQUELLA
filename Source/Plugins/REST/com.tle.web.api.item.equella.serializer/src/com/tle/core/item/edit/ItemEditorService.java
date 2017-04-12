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
