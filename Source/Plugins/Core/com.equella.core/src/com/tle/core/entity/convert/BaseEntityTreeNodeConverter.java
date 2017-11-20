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
