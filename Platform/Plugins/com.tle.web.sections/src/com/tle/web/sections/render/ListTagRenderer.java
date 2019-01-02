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

package com.tle.web.sections.render;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.tle.web.sections.SectionWriter;

@SuppressWarnings("nls")
public class ListTagRenderer extends TagRenderer
{
	private final Iterable<ListElementState> elements;

	public ListTagRenderer(TagState state, boolean ordered, Iterable<ListElementState> elements)
	{
		super(ordered ? "ol" : "ul", state);
		this.elements = elements;
	}

	public ListTagRenderer(List<?> elements)
	{
		this(new TagState(), false, convertToStates(elements));
	}

	private static Iterable<ListElementState> convertToStates(List<?> elements)
	{
		List<ListElementState> elems = Lists.newArrayList();
		for( Object object : elements )
		{
			ListElementState listElem;
			if( object instanceof ListElementState )
			{
				listElem = (ListElementState) object;
			}
			else
			{
				listElem = new ListElementState(object);
			}
			elems.add(listElem);
		}
		return elems;
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		for( ListElementState listElem : elements )
		{
			writer.render(new TagRenderer("li", listElem, listElem.getContent()));
		}
	}
}
