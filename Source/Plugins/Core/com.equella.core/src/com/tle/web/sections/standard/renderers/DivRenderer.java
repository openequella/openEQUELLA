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

package com.tle.web.sections.standard.renderers;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.model.HtmlComponentState;

@NonNullByDefault
public class DivRenderer extends TagRenderer
{
	public DivRenderer(Object innerContent)
	{
		this((String) null, innerContent);
	}

	public DivRenderer(HtmlComponentState tagState)
	{
		this((TagState) tagState);
	}

	public DivRenderer(TagState tagState)
	{
		super("div", tagState); //$NON-NLS-1$
	}

	public DivRenderer(TagState tagState, @Nullable Object innerContent)
	{
		super("div", tagState, SectionUtils.convertToRenderer(innerContent)); //$NON-NLS-1$
	}

	public DivRenderer(@Nullable String styleClass, @Nullable Object innerContent)
	{
		this(new TagState(), "div", styleClass, innerContent); //$NON-NLS-1$
	}

	public DivRenderer(TagState tagState, @Nullable String styleClass, @Nullable Object innerContent)
	{
		this(tagState, "div", styleClass, innerContent); //$NON-NLS-1$
	}

	public DivRenderer(String tag, @Nullable String styleClass, @Nullable Object innerContent)
	{
		this(new TagState(), tag, styleClass, innerContent);
	}

	public DivRenderer(TagState tagState, String tag, @Nullable String styleClass, @Nullable Object innerContent)
	{
		super(tag, tagState);
		if( !Check.isEmpty(styleClass) )
		{
			tagState.addClass(styleClass);
		}
		setNestedRenderable(SectionUtils.convertToRenderer(innerContent));
	}

	@Override
	public SectionRenderable getNestedRenderable()
	{
		if( nestedRenderable != null )
		{
			return nestedRenderable;
		}

		if( tagState instanceof HtmlComponentState )
		{
			HtmlComponentState state = (HtmlComponentState) tagState;
			if( state.getLabel() != null )
			{
				nestedRenderable = state.createLabelRenderer();
			}
		}

		return nestedRenderable;
	}
}
