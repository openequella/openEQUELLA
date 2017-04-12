package com.tle.freetext;

import com.tle.beans.Institution;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.guice.BindFactory;

@BindFactory
public interface IndexedItemFactory
{
	IndexedItem create(ItemIdKey key, Institution institution);
}
