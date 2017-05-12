package com.tle.web.sections.convert;

import net.entropysoft.transmorph.ConversionContext;
import net.entropysoft.transmorph.ConverterException;
import net.entropysoft.transmorph.converters.AbstractConverter;
import net.entropysoft.transmorph.type.TypeReference;

import com.google.gson.Gson;

public class ToJSONConverter extends AbstractConverter
{
	private final Gson gson = new Gson();

	@Override
	protected boolean canHandleDestinationType(TypeReference<?> destinationType)
	{
		return destinationType.getRawType() == String.class;
	}

	@Override
	protected boolean canHandleSourceObject(Object sourceObject)
	{
		return true;
	}

	@Override
	public Object doConvert(ConversionContext context, Object sourceObject, TypeReference<?> destinationType)
		throws ConverterException
	{
		if( sourceObject == null )
		{
			return null;
		}

		return gson.toJson(sourceObject);
	}

}
