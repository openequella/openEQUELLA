package com.tle.core.item.serializer;

import java.util.Collection;

import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

public interface ItemSerializerItemBean
{
	void writeItemBeanResult(EquellaItemBean equellaItemBean, long itemId);

	boolean hasPrivilege(long itemKey, String privilege);

	Collection<Long> getItemIds();

	<T> T getData(long itemId, String alias);
}
