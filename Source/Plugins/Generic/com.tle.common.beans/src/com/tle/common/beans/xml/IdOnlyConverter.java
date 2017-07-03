package com.tle.common.beans.xml;

import com.google.common.base.Throwables;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.beans.IdCloneable;

@SuppressWarnings("nls")
public class IdOnlyConverter implements Converter
{
	private final Class<? extends IdCloneable> clazz;

	public IdOnlyConverter(Class<? extends IdCloneable> clazz)
	{
		this.clazz = clazz;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class clazz)
	{
		return this.clazz.isAssignableFrom(clazz);
	}

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context)
	{
		writer.startNode("id");
		writer.setValue(Long.toString(((IdCloneable) obj).getId()));
		writer.endNode();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Class<? extends IdCloneable> idClass = context.getRequiredType();
		IdCloneable newIdType;
		try
		{
			newIdType = idClass.newInstance();
			reader.moveDown();
			String value = reader.getValue();
			newIdType.setId(Long.parseLong(value));
			reader.moveUp();
			return newIdType;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}
}
