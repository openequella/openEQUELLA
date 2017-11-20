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

package com.tle.core.settings.property.serialise;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public abstract class AnnotationPropertySerialiser<T extends Annotation> extends PropertySerialiser
{
	@Override
	public void query(Object object, Field field, Collection<String> queries) throws Exception
	{
		T t = field.getAnnotation(getType());
		if( t != null )
		{
			queries.add(getKey(t));
		}
	}

	@Override
	public void load(Object object, Field field, Map<String, String> properties) throws Exception
	{
		T t = field.getAnnotation(getType());
		if( t != null )
		{
			load(object, field, t, properties);
		}
	}

	@Override
	public void save(Object object, Field field, Map<String, String> properties) throws Exception
	{
		T t = field.getAnnotation(getType());
		if( t != null )
		{
			save(object, field, t, properties);
		}
	}

	abstract String getKey(T property);

	abstract Class<T> getType();

	abstract void load(Object object, Field field, T details, Map<String, String> properties) throws Exception;

	abstract void save(Object object, Field field, T details, Map<String, String> properties) throws Exception;
}
