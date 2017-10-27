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

package com.tle.core.entity.service.impl;

import java.util.Set;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.beans.entity.BaseEntity;
import com.tle.core.entity.registry.EntityRegistry;
import com.tle.core.entity.service.AbstractEntityService;

public class BaseEntityXmlConverter implements Converter
{
	private final Set<Class<? extends BaseEntity>> classes;
	private final EntityRegistry registry;

	public BaseEntityXmlConverter(EntityRegistry registry)
	{
		classes = null;
		this.registry = registry;
	}

	public BaseEntityXmlConverter(Set<Class<? extends BaseEntity>> classes, EntityRegistry registry)
	{
		this.classes = classes;
		this.registry = registry;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class clazz)
	{
		return (classes == null || !classes.contains(clazz)) && BaseEntity.class.isAssignableFrom(clazz);
	}

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context)
	{
		BaseEntity entity = (BaseEntity) obj;
		writer.addAttribute("entityclass", obj.getClass().getName()); //$NON-NLS-1$
		String uuid = entity.getUuid();
		writer.addAttribute("uuid", uuid); //$NON-NLS-1$
	}

	@Override
	@SuppressWarnings({"unchecked", "nls"})
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		String classFromStream = reader.getAttribute("entityclass"); //$NON-NLS-1$
		String uuidFromStream = reader.getAttribute("uuid"); //$NON-NLS-1$
		try
		{
			AbstractEntityService<?, ? extends BaseEntity> service = registry
				.getServiceForClass((Class<? extends BaseEntity>) Class.forName(classFromStream));
			if( service == null )
			{
				throw new RuntimeException("Could not find service for class '" + classFromStream
					+ "' in entity registry!");
			}
			return service.getByUuid(uuidFromStream);
		}
		catch( ClassNotFoundException e )
		{
			throw new RuntimeException(e);
		}
	}

}
