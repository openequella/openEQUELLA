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

package com.tle.web.sections.render;

import java.io.IOException;
import java.util.List;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;

public class FallbackTemplateResult implements TemplateResult
{
	private TemplateResult fallback;
	private TemplateResult main;

	public FallbackTemplateResult(TemplateResult main, TemplateResult fallback)
	{
		this.main = main;
		this.fallback = fallback;
	}

	@Override
	public TemplateRenderable getNamedResult(RenderContext info, final String name)
	{
		return new FallbackTemplateRenderable(name);
	}

	public class FallbackTemplateRenderable extends AbstractCombinedTemplateRenderable
	{
		private String name;

		public FallbackTemplateRenderable(String name)
		{
			this.name = name;
		}

		@Override
		public void preRender(PreRenderContext info)
		{
			List<TemplateRenderable> renderers = getRenderers(info);
			for( TemplateRenderable templateRenderable : renderers )
			{
				if( templateRenderable != null && templateRenderable.exists(info) )
				{
					info.preRender(templateRenderable);
					return;
				}
			}
		}

		@Override
		public void realRender(SectionWriter writer) throws IOException
		{
			List<TemplateRenderable> renderers = getRenderers(writer);
			for( TemplateRenderable templateRenderable : renderers )
			{
				if( templateRenderable != null && templateRenderable.exists(writer) )
				{
					templateRenderable.realRender(writer);
					return;
				}
			}
		}

		@Override
		protected void setupTemplateRenderables(RenderContext context)
		{
			addTemplateRenderable(main.getNamedResult(context, name));
			addTemplateRenderable(fallback.getNamedResult(context, name));
		}

	}
}
