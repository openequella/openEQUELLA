/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.registry.handler;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dytech.edge.exceptions.WebException;
import com.tle.common.Check;
import com.tle.common.util.CachedPropertyInfo;
import com.tle.web.sections.BookmarkContextHolder;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SimpleSectionId;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.convert.Conversion;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.registry.handler.util.FieldAccessor;
import com.tle.web.sections.registry.handler.util.MethodAccessor;
import com.tle.web.sections.registry.handler.util.PropertyAccessor;

public class AnnotatedBookmarkScanner
{
	public static class BookmarkedProperties
	{
		PropertyAccessor readAccessor;
		PropertyAccessor writeAccessor;
		Bookmarked annotation;
		BookmarkContextHolder contexts;
		boolean withDot;
		String name;
		String legacyName;
	}

	private final List<BookmarkedProperties> properties;
	private boolean hasRendered;
	private final Conversion conversion;

	public AnnotatedBookmarkScanner(Class<?> clazz, BookmarkRegistrationHandler handler, Conversion conversion)
	{
		this.conversion = conversion;
		properties = new ArrayList<BookmarkedProperties>();
		CachedPropertyInfo cachedBeanInfo = CachedPropertyInfo.forClass(clazz);
		Bookmarked classAnn = clazz.getAnnotation(Bookmarked.class);
		Field[] fields = clazz.getDeclaredFields();
		for( Field field : fields )
		{
			Bookmarked bookmark = field.getAnnotation(Bookmarked.class);
			if( bookmark != null )
			{
				addProperty(cachedBeanInfo.getPropertyDescriptor(field.getName()), field, bookmark, classAnn);
			}
		}
		Method[] methods = clazz.getDeclaredMethods();
		for( Method method : methods )
		{
			Bookmarked bookmark = method.getAnnotation(Bookmarked.class);
			if( bookmark != null )
			{
				addProperty(cachedBeanInfo.findPropertyForMethod(method), null, bookmark, classAnn);
			}
		}
		clazz = clazz.getSuperclass();
		if( clazz != null )
		{
			AnnotatedBookmarkScanner scanner = handler.getForClass(clazz);
			properties.addAll(scanner.properties);
			hasRendered |= scanner.hasRendered;
		}
	}

	private void addProperty(PropertyDescriptor descriptor, Field field, Bookmarked bookmark, Bookmarked classAnn)
	{
		BookmarkedProperties props = new BookmarkedProperties();
		BookmarkContextHolder contextHolder = new BookmarkContextHolder();
		Set<String> contexts = new HashSet<String>(Arrays.asList(bookmark.contexts()));
		Set<String> onlyForContexts = new HashSet<String>(Arrays.asList(bookmark.onlyForContext()));
		Set<String> ignoreForContexts = new HashSet<String>(Arrays.asList(bookmark.ignoreForContext()));
		if( classAnn != null )
		{
			Collections.addAll(contexts, classAnn.contexts());
			Collections.addAll(onlyForContexts, classAnn.onlyForContext());
			Collections.addAll(ignoreForContexts, classAnn.ignoreForContext());
		}
		contexts.addAll(onlyForContexts);
		if( bookmark.supported() )
		{
			contexts.add(BookmarkEvent.CONTEXT_SUPPORTED);
		}
		contextHolder.setContexts(contexts);
		contextHolder.setOnlyForContext(onlyForContexts);
		contextHolder.setIgnoreForContext(ignoreForContexts);
		props.contexts = contextHolder;
		if( descriptor != null && descriptor.getReadMethod() != null )
		{
			props.readAccessor = new MethodAccessor(descriptor);
		}
		else if( field != null )
		{
			props.readAccessor = new FieldAccessor(field);
		}
		if( descriptor != null && descriptor.getWriteMethod() != null )
		{
			props.writeAccessor = new MethodAccessor(descriptor);
		}
		else if( field != null )
		{
			props.writeAccessor = new FieldAccessor(field);
		}
		props.annotation = bookmark;
		String name = bookmark.parameter();
		if( name.isEmpty() )
		{
			props.withDot = true;
			name = bookmark.name();
		}
		if( name.isEmpty() )
		{
			if( descriptor == null )
			{
				if( field == null )
				{
					// Not possible I'd say
					throw new Error("field and descriptor are both null");
				}
				name = field.getName();
			}
			else
			{
				name = descriptor.getName();
			}
		}
		props.name = name;
		String legacy = bookmark.legacyName();
		if( !Check.isEmpty(legacy) )
		{
			props.legacyName = legacy;
		}
		hasRendered |= bookmark.rendered();

		properties.add(props);
	}

	public boolean hasAnnotations()
	{
		return !properties.isEmpty();
	}

	public void handleParameters(SectionInfo info, String prefix, Object model, ParametersEvent event)
		throws WebException
	{
		for( BookmarkedProperties prop : properties )
		{
			String[] vals = null;
			String name = prop.name;
			String legacyName = prop.legacyName;
			if( prefix.equals(".") && prop.withDot ) //$NON-NLS-1$
			{
				vals = event.getParameterValues(name);
				if( vals == null && legacyName != null )
				{
					vals = event.getParameterValues(legacyName);
				}
			}
			if( vals == null )
			{
				String paramId;
				if( prop.withDot )
				{
					paramId = prefix + name;
				}
				else
				{
					paramId = name;
				}
				vals = event.getParameterValues(paramId);
				if( vals == null && legacyName != null )
				{
					vals = event.getParameterValues(prefix + legacyName);
				}
			}
			if( vals != null )
			{
				Object val = null;
				try
				{
					val = conversion.convertFromParameters(name, prop.writeAccessor.getType(), vals);
				}
				catch( WebException wex )
				{
					// HACK: Dodgy calls may be caused by redundancies in the
					// POST data, where instead of key/value of
					// x=["a value for x"], converting array[1] to string,
					// we get x=["a value for x", "a value for x"]
					if( vals.length == 2 && vals[0].equals(vals[1]) )
					{
						String[] repairedVals = new String[1];
						repairedVals[0] = vals[0];
						val = conversion.convertFromParameters(name, prop.writeAccessor.getType(), repairedVals);
					}
					else
					{
						throw new RuntimeException(wex);
					}
				}
				try
				{
					prop.writeAccessor.write(model, val);
				}
				catch( Exception e )
				{
					throw new RuntimeException(e);
				}
				try
				{
					prop.writeAccessor.write(model, val);
				}
				catch( Exception e )
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

	public void bookmark(SectionInfo info, String id, BookmarkEvent event)
	{
		Object model = info.getModelForId(id);
		String prefix = id + '.';
		for( BookmarkedProperties prop : properties )
		{
			Bookmarked annotation = prop.annotation;
			if( isAppropriate(prop, annotation, event, info, id) )
			{
				Object val = null;
				try
				{
					if( prop.readAccessor != null )
					{
						val = prop.readAccessor.read(model);
					}
				}
				catch( Exception e )
				{
					throw new RuntimeException(e);
				}
				String paramId;
				if( prop.withDot )
				{
					paramId = prefix + prop.name;
				}
				else
				{
					paramId = prop.name;
				}
				if( !ignoreValue(val, annotation) )
				{
					event.setParams(paramId, conversion.convertToParameters(val));
				}
				else
				{
					event.setParam(paramId, null);
				}
			}
		}
	}

	private boolean ignoreValue(Object val, Bookmarked annotation)
	{
		if( val == null )
		{
			return true;
		}
		if( !annotation.nodefault() && SectionUtils.isDefaultValue(val) )
		{
			return true;
		}
		if( annotation.ignoreEmpty() && val instanceof String && Check.isEmpty((String) val) )
		{
			return true;
		}
		return false;
	}

	private boolean isAppropriate(BookmarkedProperties prop, Bookmarked annotation, BookmarkEvent event,
		SectionInfo info, String id)
	{
		if( !event.isRendering() && !annotation.stateful() )
		{
			return false;
		}
		if( event.isRendering() && annotation.rendered() && SectionUtils.hasBeenRendered(info, id) )
		{
			return false;
		}
		return event.isAllowedInThisContext(prop.contexts);

	}

	public boolean hasRendered()
	{
		return hasRendered;
	}

	public void document(SectionInfo info, String id, DocumentParamsEvent event)
	{
		String prefix = id + '.';
		for( BookmarkedProperties prop : properties )
		{
			if( prop.annotation.stateful() )
			{
				String paramId;
				if( prop.withDot )
				{
					paramId = prefix + prop.name;
				}
				else
				{
					paramId = prop.name;
				}
				String type = prop.readAccessor.getType().toString();
				event.addParam(prop.contexts.getContexts().contains(BookmarkEvent.CONTEXT_SUPPORTED), paramId, type);
			}
		}

	}

	public void registerConverters(String id, SectionTree tree)
	{
		SimpleSectionId sectionId = new SimpleSectionId(id);
		for( BookmarkedProperties prop : properties )
		{
			conversion.registerBookmark(tree, sectionId, prop.readAccessor, prop.writeAccessor);
		}
	}
}
