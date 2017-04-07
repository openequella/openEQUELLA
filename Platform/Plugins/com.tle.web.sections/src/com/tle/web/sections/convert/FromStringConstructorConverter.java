package com.tle.web.sections.convert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.entropysoft.transmorph.ConversionContext;
import net.entropysoft.transmorph.ConverterException;
import net.entropysoft.transmorph.converters.AbstractConverter;
import net.entropysoft.transmorph.type.TypeReference;

import com.google.common.base.Throwables;

public class FromStringConstructorConverter extends AbstractConverter
{
	private final Map<Class<?>, Constructor<?>> constructors = Collections
		.synchronizedMap(new HashMap<Class<?>, Constructor<?>>());

	@Override
	protected boolean canHandleDestinationType(TypeReference<?> destinationType)
	{
		Class<?> rawType = destinationType.getRawType();
		if( !constructors.containsKey(rawType) )
		{
			try
			{
				constructors.put(rawType, rawType.getConstructor(String.class));
			}
			catch( NoSuchMethodException e )
			{
				constructors.put(rawType, null);
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
		return constructors.get(rawType) != null;
	}

	@Override
	protected boolean canHandleSourceObject(Object sourceObject)
	{
		return sourceObject instanceof String;
	}

	@Override
	public Object doConvert(ConversionContext context, Object sourceObject, TypeReference<?> destinationType)
		throws ConverterException
	{
		Constructor<?> cons = constructors.get(destinationType.getRawType());
		try
		{
			return cons.newInstance(sourceObject);
		}
		catch( InvocationTargetException t )
		{
			if( t.getTargetException() instanceof ConvertedToNull )
			{
				return null;
			}
			throw Throwables.propagate(t.getTargetException());
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

}
