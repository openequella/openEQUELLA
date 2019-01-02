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

package com.tle.web.sections.ajax;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;

public class AjaxTagRenderer extends TagRenderer
{
	private boolean includeTag;

	public AjaxTagRenderer(String tag, TagState state, SectionRenderable nestedRenderable, boolean includeTag)
	{
		super(tag, state, nestedRenderable);
		this.includeTag = includeTag;
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		if( !isCapture(writer) )
		{
			super.realRender(writer);
			return;
		}
		AjaxRenderContext context = writer.getAttributeForClass(AjaxRenderContext.class);
		if( context == null || context.isCurrentlyCapturing() )
		{
			super.realRender(writer);
		}
		else
		{
			if( includeTag )
			{
				writer = startCapture(context, writer);
			}
			Map<String, String> attrs = prepareAttributes(writer);
			writeStart(writer, attrs);
			if( !includeTag )
			{
				writeMiddle(startCapture(context, writer));
				endCapture(context);
			}
			else
			{
				writeMiddle(writer);
			}
			writeEnd(writer);
			if( includeTag )
			{
				endCapture(context);
			}
		}
	}

	protected boolean isCapture(SectionWriter writer)
	{
		return true;
	}

	private void endCapture(AjaxRenderContext context)
	{
		context.endCapture(getAjaxDivId(context));
	}

	private SectionWriter startCapture(AjaxRenderContext context, SectionWriter writer)
	{
		String divId = getAjaxDivId(writer);
		context.addAjaxDivs(divId);
		return new SectionWriter(context.startCapture(writer, divId, getCaptureParams(writer), isCollection(writer)),
			writer);
	}

	protected Map<String, Object> getCaptureParams(RenderContext context)
	{
		return ImmutableMap.of();
	}

	protected boolean isCollection(RenderContext context)
	{
		return false;
	}

	protected String getAjaxDivId(RenderContext context)
	{
		return getElementId(context);
	}

}
