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
