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

package com.tle.web.sections.render;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.ElementId;

public class HiddenInput implements SectionRenderable
{
	private final List<HiddenValue> hiddenFields;

	public HiddenInput()
	{
		hiddenFields = new ArrayList<HiddenValue>();
	}

	public HiddenInput(ElementId id, String val)
	{
		this(id, null, val);
	}

	public HiddenInput(String name, String val)
	{
		this(null, name, val);
	}

	public HiddenInput(ElementId id, String name, String val)
	{
		hiddenFields = Arrays.asList(new HiddenValue(id, name, val));
	}

	public HiddenInput(Map<String, String[]> stateMap)
	{
		hiddenFields = new ArrayList<HiddenValue>();
		for( Map.Entry<String, String[]> entry : stateMap.entrySet() )
		{
			String name = entry.getKey();
			for( String val : entry.getValue() )
			{
				addField(null, name, val);
			}
		}
	}

	public void addField(ElementId id, String name, String val)
	{
		hiddenFields.add(new HiddenValue(id, name, val));
	}

	@Override
	public void preRender(PreRenderContext writer)
	{
		// nothing
	}

	@SuppressWarnings("nls")
	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		try
		{
			Map<String, String> attrs = new LinkedHashMap<String, String>();
			attrs.put("type", "hidden");
			for( HiddenValue nv : hiddenFields )
			{
				String name = nv.name;
				String id = null;
				if( nv.id != null )
				{
					id = nv.id.getElementId(writer);
				}
				if( name == null )
				{
					name = id;
				}
				if( id != null )
				{
					attrs.put("id", id);
				}
				attrs.put("name", name);
				attrs.put("value", nv.value);
				writer.writeTag("input", attrs);
				writer.append('\n');
			}
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	private static class HiddenValue
	{
		final ElementId id;
		final String name;
		final String value;

		public HiddenValue(ElementId id, String name, String value)
		{
			this.id = id;
			this.name = name;
			this.value = value;
		}

	}
}
