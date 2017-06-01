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
