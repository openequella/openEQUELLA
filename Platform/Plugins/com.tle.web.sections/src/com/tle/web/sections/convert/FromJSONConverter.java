package com.tle.web.sections.convert;

import net.entropysoft.transmorph.ConversionContext;
import net.entropysoft.transmorph.ConverterException;
import net.entropysoft.transmorph.converters.AbstractContainerConverter;
import net.entropysoft.transmorph.type.TypeReference;

import com.google.gson.Gson;

public class FromJSONConverter extends AbstractContainerConverter
{
	private Gson gson = new Gson();

	@Override
	protected boolean canHandleDestinationType(TypeReference<?> destinationType)
	{
		return !(destinationType.isPrimitive() || destinationType.isNumber());
	}

	@Override
	protected boolean canHandleSourceObject(Object sourceObject)
	{
		return (sourceObject instanceof String);
	}

	@Override
	public Object doConvert(ConversionContext context, Object sourceObject, TypeReference<?> destinationType)
		throws ConverterException
	{
		return gson.fromJson((String) sourceObject, destinationType.getType());
	}
}
