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

package com.tle.web.sections.ajax;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.SectionRenderable;

@NonNullByDefault
public class AjaxCaptureRenderer implements SectionRenderable
{
	private final String divId;
	private final SectionRenderable renderer;
	private final Map<String, Object> params;

	public AjaxCaptureRenderer(String divId, SectionRenderable renderer)
	{
		this(divId, renderer, null);
	}

	public AjaxCaptureRenderer(String divId, SectionRenderable renderer, Map<String, Object> params)
	{
		this.divId = divId;
		this.renderer = renderer;
		this.params = params;
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		AjaxRenderContext ajaxContext = writer.getAttributeForClass(AjaxRenderContext.class);
		if( ajaxContext != null )
		{
			Writer newWriter = ajaxContext.startCapture(writer, divId, params, false);
			if( !newWriter.equals(writer) )
			{
				writer = new SectionWriter(newWriter, writer);
			}
		}
		writer.preRender(renderer);
		renderer.realRender(writer);
		if( ajaxContext != null )
		{
			ajaxContext.endCapture(divId);
		}
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// nothing
	}
}
