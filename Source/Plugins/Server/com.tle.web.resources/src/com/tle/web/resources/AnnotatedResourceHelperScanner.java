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

package com.tle.web.resources;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionUtils;

public class AnnotatedResourceHelperScanner
{
	private static class ResourceData
	{
		Field field;
		Class<?> clazz;
	}

	private final Class<?> clazz;
	private PluginResourceHelper helper;
	private final List<ResourceData> fieldData = new ArrayList<ResourceData>();
	private final ResourceHelperHandler handler;

	public AnnotatedResourceHelperScanner(Class<?> clazz, ResourceHelperHandler handler)
	{
		this.handler = handler;
		this.clazz = clazz;
		try
		{
			Field[] fields = clazz.getDeclaredFields();
			for( Field field : fields )
			{
				ResourceHelper annotation = field.getAnnotation(ResourceHelper.class);
				if( annotation != null )
				{
					field.setAccessible(true);
					if( (field.getModifiers() & Modifier.STATIC) != 0 )
					{
						field.set(null, getHelper());
					}
					else
					{
						ResourceData data = new ResourceData();
						data.field = field;
						if( annotation.fixed() )
						{
							data.clazz = clazz;
						}
						fieldData.add(data);
					}
				}
			}
			clazz = clazz.getSuperclass();
			if( clazz != null )
			{
				AnnotatedResourceHelperScanner scanner = handler.getForClass(clazz);
				fieldData.addAll(scanner.fieldData);
			}
		}
		catch( IllegalAccessException e )
		{
			SectionUtils.throwRuntime(e);
		}
	}

	private PluginResourceHelper getHelper()
	{
		if( helper == null )
		{
			helper = ResourcesService.getResourceHelper(clazz);
		}
		return helper;
	}

	public void setup(Section section)
	{
		try
		{
			for( ResourceData data : fieldData )
			{
				PluginResourceHelper tempHelper;
				if( data.clazz != null )
				{
					tempHelper = handler.getForClass(data.clazz).getHelper();
				}
				else
				{
					tempHelper = getHelper();
				}
				data.field.set(section, tempHelper);
			}
		}
		catch( IllegalAccessException e )
		{
			SectionUtils.throwRuntime(e);
		}
	}
}
