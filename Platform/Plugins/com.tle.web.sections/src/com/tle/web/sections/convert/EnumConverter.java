package com.tle.web.sections.convert;

import net.entropysoft.transmorph.ConversionContext;
import net.entropysoft.transmorph.ConverterException;
import net.entropysoft.transmorph.converters.AbstractConverter;
import net.entropysoft.transmorph.type.TypeReference;

public class EnumConverter extends AbstractConverter
{
	@Override
	public Object doConvert(ConversionContext context, Object sourceObject, TypeReference<?> destinationType)
		throws ConverterException
	{
		if( sourceObject == null )
		{
			return null;
		}
		return ((Enum<?>) sourceObject).name();
	}

	@Override
	protected boolean canHandleSourceObject(Object sourceObject)
	{
		return (sourceObject == null || sourceObject.getClass().isEnum());
	}

	@Override
	protected boolean canHandleDestinationType(TypeReference<?> destinationType)
	{
		return destinationType.getRawType() == String.class;
	}
}
