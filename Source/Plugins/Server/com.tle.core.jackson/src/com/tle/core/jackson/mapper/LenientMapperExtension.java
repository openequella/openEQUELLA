package com.tle.core.jackson.mapper;

import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.MapperExtension;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class LenientMapperExtension implements MapperExtension
{
	public static final String NAME = "lenient";

	@Override
	public void extendMapper(ObjectMapper mapper)
	{
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}
}
