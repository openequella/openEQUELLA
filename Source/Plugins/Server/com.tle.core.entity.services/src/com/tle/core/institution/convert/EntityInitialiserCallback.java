package com.tle.core.institution.convert;

import com.tle.beans.entity.BaseEntity;
import com.tle.core.initialiser.InitialiserCallback;
import com.tle.core.initialiser.Property;

public class EntityInitialiserCallback implements InitialiserCallback
{
	@Override
	public void set(Object obj, Property property, Object value)
	{
		if( value instanceof BaseEntity )
		{
			BaseEntity toset = (BaseEntity) value;
			toset.setUuid(((BaseEntity) property.get(obj)).getUuid());
		}
		property.set(obj, value);
	}

	@Override
	public void entitySimplified(Object old, Object newObj)
	{
		if( old instanceof BaseEntity )
		{
			BaseEntity toset = (BaseEntity) newObj;
			BaseEntity oldObj = (BaseEntity) old;
			toset.setUuid(oldObj.getUuid());
		}
	}
}
