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

package com.tle.web.freemarker.annotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.tle.core.plugins.PluginService;
import com.tle.web.freemarker.CustomTemplateLoader;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsRuntimeException;

public class AnnotatedViewFactoryScanner
{
	public static class ViewFactoryData
	{
		ViewFactory annotation;
		Field field;
	}

	private transient List<ViewFactoryData> viewFactoryFields = new ArrayList<ViewFactoryData>();

	public AnnotatedViewFactoryScanner(final Class<?> clazz, final FreemarkerFactoryHandler handler)
	{
		Field[] fields = clazz.getDeclaredFields();
		for( Field field : fields )
		{
			ViewFactory annotation = field.getAnnotation(ViewFactory.class);
			if( annotation != null )
			{
				field.setAccessible(true);

				ViewFactoryData data = new ViewFactoryData();
				data.field = field;
				data.annotation = annotation;

				viewFactoryFields.add(data);
			}
		}

		Class<?> superClazz = clazz.getSuperclass();
		if( superClazz != null )
		{
			AnnotatedViewFactoryScanner scanner = handler.getForClass(superClazz);
			viewFactoryFields.addAll(scanner.viewFactoryFields);
		}
	}

	public void setupFactories(Section section, CustomTemplateLoader templateLoader, PluginService pluginService)
	{
		for( ViewFactoryData data : viewFactoryFields )
		{
			final ViewFactory annotation = data.annotation;
			final Field field = data.field;

			String factoryId = annotation.name();
			if( annotation.fixed() )
			{
				factoryId = pluginService.getPluginIdForObject(field.getDeclaringClass()) + '@' + factoryId;
			}
			else if( factoryId.indexOf('@') == -1 )
			{
				factoryId = pluginService.getPluginIdForObject(section) + '@' + factoryId;
			}

			FreemarkerFactory factory = templateLoader.getFactoryForName(factoryId);
			if( factory == null )
			{
				if( !annotation.optional() )
				{
					throw new SectionsRuntimeException("No factory for id:'" + factoryId + "':" + field); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			try
			{
				field.set(section, factory);
			}
			catch( Exception e )
			{
				SectionUtils.throwRuntime(e);
			}
		}
	}
}
