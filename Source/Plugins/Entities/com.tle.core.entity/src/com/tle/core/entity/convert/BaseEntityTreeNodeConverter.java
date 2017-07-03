package com.tle.core.entity.convert;

import javax.inject.Inject;

import com.thoughtworks.xstream.XStream;
import com.tle.common.institution.TreeNodeInterface;
import com.tle.core.entity.registry.EntityRegistry;
import com.tle.core.entity.service.impl.BaseEntityXmlConverter;
import com.tle.core.entity.service.impl.EntityInitialiserCallback;
import com.tle.core.hibernate.equella.service.InitialiserCallback;
import com.tle.core.hibernate.equella.service.Property;
import com.tle.core.institution.convert.TreeNodeConverter;

/**
 * @author Aaron
 *
 */
public abstract class BaseEntityTreeNodeConverter<T extends TreeNodeInterface<T>> extends TreeNodeConverter<T>
{
	@Inject
	private EntityRegistry registry;

	public BaseEntityTreeNodeConverter(String folder, String oldSingleFilename)
	{
		super(folder, oldSingleFilename);
	}

	@Override
	protected XStream createXStream()
	{
		XStream x = super.createXStream();
		x.registerConverter(new BaseEntityXmlConverter(registry));
		return x;
	}

	@Override
	protected InitialiserCallback createInitialiserCallback()
	{
		return new BaseEntityTreeInitialiserCallback<T>();
	}

	protected static class BaseEntityTreeInitialiserCallback<T extends TreeNodeInterface<T>>
		extends
			EntityInitialiserCallback
	{
		@Override
		@SuppressWarnings("unchecked")
		public void set(Object obj, Property property, Object value)
		{
			if( value instanceof TreeNodeInterface )
			{
				T toset = (T) value;
				toset.setUuid(((T) property.get(obj)).getUuid());
			}
			super.set(obj, property, value);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void entitySimplified(Object old, Object newObj)
		{
			if( old instanceof TreeNodeInterface )
			{
				T toset = (T) newObj;
				T oldObj = (T) old;
				toset.setUuid(oldObj.getUuid());
			}
			super.entitySimplified(old, newObj);
		}
	}
}
