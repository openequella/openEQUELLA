package com.tle.core.services.entity;

import java.util.Set;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.beans.entity.BaseEntity;

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
