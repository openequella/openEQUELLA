/*
 * Created on 18/04/2006
 */
package com.tle.core.remoting;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;

public interface RemoteItemService
{
	Item get(ItemKey key);

	String getNameForId(long id);

	String getAsXml(ItemId key);
}
