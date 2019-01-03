/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.convert;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import net.entropysoft.transmorph.ConverterException;
import net.entropysoft.transmorph.IConverter;
import net.entropysoft.transmorph.Transmorph;
import net.entropysoft.transmorph.converters.MultiConverter;
import net.entropysoft.transmorph.converters.SingleElementToArray;
import net.entropysoft.transmorph.converters.collections.ArrayToArray;
import net.entropysoft.transmorph.converters.collections.ArrayToCollection;
import net.entropysoft.transmorph.converters.collections.ArrayToSingleElement;
import net.entropysoft.transmorph.converters.collections.CollectionToArray;
import net.entropysoft.transmorph.converters.collections.CollectionToCollection;
import net.entropysoft.transmorph.type.TypeReference;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.dytech.edge.exceptions.BadRequestException;
import com.dytech.edge.exceptions.WebException;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SimpleSectionId;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.registry.handler.util.PropertyAccessor;

// Must be eager singleton
public class Conversion
{
	private static Conversion inst;

	public enum ConversionType
	{
		TOPARAMS, FROMPARAMS, FROMJS, TOJS
	}

	private Transmorph morphToParams;
	private Transmorph morphFromParams;
	private Transmorph morphFromJSString;
	private Transmorph morphToJSExpression;

	@Inject
	private PluginTracker<SectionsConverter> tracker;
	private List<SectionsConverter> allConverters;

	public Conversion()
	{
		inst = this;
	}

	public static Conversion inst()
	{
		return inst;
	}

	public synchronized void ensureConverters()
	{
		if( tracker.needsUpdate() || morphToParams == null )
		{
			List<IConverter> converterList = new ArrayList<IConverter>();
			converterList.add(new DefaultObjectsToString());
			addCustom(converterList, ConversionType.TOPARAMS);
			converterList.add(new ConstructorToStringConverter());
			converterList.add(new CollectionToArray());
			converterList.add(new ArrayToArray());
			converterList.add(new ToJSONConverter());
			converterList.add(new SingleElementToArray());
			morphToParams = new Transmorph(converterList.toArray(new IConverter[converterList.size()]));

			converterList = new ArrayList<IConverter>();
			converterList.add(new DefaultFromStringConverter());
			addCustom(converterList, ConversionType.FROMPARAMS);
			converterList.add(new FromStringConstructorConverter());
			converterList.add(new ArrayToCollection());
			converterList.add(new ArrayToArray());
			converterList.add(new FromJSONConverter());
			converterList.add(new ArrayToSingleElement());
			morphFromParams = new Transmorph(converterList.toArray(new IConverter[converterList.size()]));

			converterList = new ArrayList<IConverter>();
			converterList.add(new DefaultFromStringConverter());
			addCustom(converterList, ConversionType.FROMJS);
			converterList.add(new FromStringConstructorConverter());
			converterList.add(new CollectionToCollection());
			converterList.add(new CollectionToArray());
			converterList.add(new FromJSONConverter());
			morphFromJSString = new Transmorph(converterList.toArray(new IConverter[converterList.size()]));

			converterList = new ArrayList<IConverter>();
			converterList.add(new DefaultObjectsToString());
			addCustom(converterList, ConversionType.TOJS);
			converterList.add(new ConstructorToStringConverter());
			converterList.add(new ToJSONConverter());
			MultiConverter toJSString = new MultiConverter(false, converterList.toArray(new IConverter[converterList
				.size()]));

			morphToJSExpression = new Transmorph(new StandardExpressions(toJSString), new CollectionToArray(),
				new ArrayToArray());
		}
	}

	private void addCustom(List<IConverter> converterList, ConversionType type)
	{
		for( SectionsConverter converter : getAllConverters() )
		{
			if( converter.supports(type.name()) )
			{
				converterList.add(converter);
			}
		}
	}

	private synchronized List<SectionsConverter> getAllConverters()
	{
		if( tracker.needsUpdate() || allConverters == null )
		{
			allConverters = new ArrayList<SectionsConverter>();
			List<Extension> ext = tracker.getExtensions();
			for( Extension extension : ext )
			{
				Collection<Parameter> params = extension.getParameters("converter"); //$NON-NLS-1$
				for( Parameter param : params )
				{
					allConverters.add(tracker.getBeanByParameter(extension, param));
				}
			}
		}
		return allConverters;
	}

	public Object convertFromParameters(String param, Type type, String[] vals) throws WebException
	{
		ensureConverters();
		try
		{
			return morphFromParams.convert(vals, type);
		}
		catch( ConverterException e )
		{
			throw new BadRequestException(param);
		}
	}

	public String[] convertToParameters(Object obj)
	{
		ensureConverters();
		try
		{
			return morphToParams.convert(obj, String[].class);
		}
		catch( ConverterException e )
		{
			throw new RuntimeException(e);
		}
	}

	public Object convertFromString(String param, Type class1)
	{
		ensureConverters();
		try
		{
			return morphFromJSString.convert(param, class1);
		}
		catch( ConverterException e )
		{
			throw new RuntimeException(e);
		}
	}

	public JSExpression convertToJSExpression(Object param)
	{
		ensureConverters();
		try
		{
			return morphToJSExpression.convert(param, JSExpression.class);
		}
		catch( ConverterException e )
		{
			throw new RuntimeException(e);
		}
	}

	public void registerBookmark(SectionTree tree, SimpleSectionId sectionId, PropertyAccessor readAccessor,
		PropertyAccessor writeAccessor)
	{
		TypeReference<?> typeRef = TypeReference.get(readAccessor.getType());
		List<SectionsConverter> customConverters = getAllConverters();
		for( SectionsConverter converter : customConverters )
		{
			converter.registerBookmark(tree, sectionId, readAccessor, writeAccessor, typeRef);
		}
	}
}
