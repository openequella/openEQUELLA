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

package com.tle.web.sections.standard.renderers.toggle;

import java.io.IOException;
import java.util.Map;

import com.tle.common.Check;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlBooleanState;

@SuppressWarnings("nls")
public class ImageTogglerRenderer extends AbstractHiddenToggler
{
	private static final String CSS_URL = ResourcesService.getResourceHelper(ImageTogglerRenderer.class).url(
		"css/toggler.css");

	public ImageTogglerRenderer(HtmlBooleanState bstate)
	{
		super(bstate);
		addClass("imageToggler");
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		super.prepareFirstAttributes(writer, attrs);
		if( bstate.isChecked() )
		{
			addClass(attrs, "imageTogglerChecked");
		}
		else
		{
			addClass(attrs, "imageTogglerUnchecked");
		}
		SectionRenderable renderable = getNestedRenderable();
		if( renderable != null )
		{
			String altText = SectionUtils.renderToString(writer.getInfo(), renderable);
			if( !Check.isEmpty(altText) )
			{
				attrs.put("alt", altText);
			}
		}
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		info.addCss(CSS_URL);
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		// nothing
	}

	@Override
	protected void writeEnd(SectionWriter writer) throws IOException
	{
		super.writeEnd(writer);

		// This is to work around a Chrome/Safari rendering issue - see #3058
		writer.write("&nbsp;");
	}
}
