package com.tle.core.item.edit;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.Item;

public interface ItemMetadataListener
{
	void metadataChanged(Item item, PropBagEx itemXml);
}
