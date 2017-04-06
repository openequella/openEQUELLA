package com.tle.core.jackson.mapper;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.MapperExtension;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class JaxbMapperExtension implements MapperExtension
{
	public static final String NAME = "jaxb";

	@Override
	public void extendMapper(ObjectMapper mapper)
	{
		// TODO: requires a new jar
		// AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
		// AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
		// AnnotationIntrospector pair = new
		// AnnotationIntrospector.Pair(primary, secondary);
		// mapper.setDeserializationConfig(mapper.getDeserializationConfig().withAnnotationIntrospector(pair));
		// mapper.setSerializationConfig(mapper.getSerializationConfig().withAnnotationIntrospector(pair));
	}
}
