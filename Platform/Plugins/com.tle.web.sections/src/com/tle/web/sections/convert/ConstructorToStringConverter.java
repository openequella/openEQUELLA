package com.tle.web.sections.convert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.entropysoft.transmorph.ConversionContext;
import net.entropysoft.transmorph.ConverterException;
import net.entropysoft.transmorph.converters.AbstractConverter;
import net.entropysoft.transmorph.type.TypeReference;

public class ConstructorToStringConverter extends AbstractConverter
{
	private final Set<Class<?>> allowedClasses = Collections.synchronizedSet(new HashSet<Class<?>>());
	private final Set<Class<?>> disallowedClasses = Collections.synchronizedSet(new HashSet<Class<?>>());

	@Override
	public Object doConvert(ConversionContext context, Object sourceObject, TypeReference<?> destinationType)
		throws ConverterException
	{
		if( sourceObject == null )
		{
			return null;
		}
		return sourceObject.toString();
	}

	@Override
	protected boolean canHandleDestinationType(TypeReference<?> destinationType)
	{
		return destinationType.isType(String.class);
	}

	@Override
	protected boolean canHandleSourceObject(Object sourceObject)
	{
		if( sourceObject == null )
		{
			return true;
		}
		Class<? extends Object> srcClass = sourceObject.getClass();
		if( allowedClasses.contains(srcClass) )
		{
			return true;
		}
		if( disallowedClasses.contains(srcClass) )
		{
			return false;
		}
		try
		{
			srcClass.getConstructor(String.class);
			allowedClasses.add(srcClass);
			return true;
		}
		catch( NoSuchMethodException e )
		{
			disallowedClasses.add(srcClass);
			return false;
		}
	}
}
