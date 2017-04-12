package com.tle.web.remoting.impl;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.Item;
import com.tle.core.initialiser.InitialiserCallback;
import com.tle.core.initialiser.Property;

public class RemoteSimplifier implements InitialiserCallback
{
	@Override
	public void entitySimplified(Object old, Object newObj)
	{
		if( old instanceof Item )
		{
			Item item = (Item) old;
			Item newItem = (Item) newObj;
			newItem.setId(item.getId());
			newItem.setUuid(item.getUuid());
			newItem.setVersion(item.getVersion());
			newItem.setName(LanguageBundle.clone(item.getName()));
			newItem.setDescription(LanguageBundle.clone(item.getDescription()));
			newItem.setStatus(item.getStatus());
		}
	}

	@Override
	public void set(Object obj, Property property, Object value)
	{
		property.set(obj, value);
	}
}
