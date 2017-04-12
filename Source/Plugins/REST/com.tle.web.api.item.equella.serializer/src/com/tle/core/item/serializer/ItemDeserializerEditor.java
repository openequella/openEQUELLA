package com.tle.core.item.serializer;

import com.tle.beans.item.Item;
import com.tle.core.item.edit.ItemEditor;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

public interface ItemDeserializerEditor
{
	void edit(EquellaItemBean itemBean, ItemEditor editor, boolean importing);

	void processFiles(Item item, ItemEditor editor, boolean importing);
}
