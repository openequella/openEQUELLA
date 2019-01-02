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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;

public class CombinedTemplateResult extends GenericTemplateResult
{
	private List<TemplateResult> others = new ArrayList<TemplateResult>();

	public CombinedTemplateResult(TemplateResult... others)
	{
		this.others.addAll(Arrays.asList(others));
	}

	@Override
	public TemplateRenderable getNamedResult(RenderContext info, final String name)
	{
		return new CombinedTemplateRenderable(name);
	}

	protected TemplateRenderable getInnerResult(RenderContext info, String name)
	{
		return super.getNamedResult(info, name);
	}

	public void addOtherTemplate(TemplateResult result)
	{
		others.add(result);
	}

	@Override
	public GenericTemplateResult addNamedResult(String name, SectionRenderable result)
	{
		if( others.isEmpty() )
		{
			super.addNamedResult(name, result);
		}
		else
		{
			others.add(new GenericTemplateResult(new GenericNamedResult(name, result)));
		}
		return this;
	}

	public void addResult(String name, SectionResult sectionResult)
	{
		if( sectionResult == null )
		{
			return;
		}
		if( sectionResult instanceof TemplateResult )
		{
			addOtherTemplate((TemplateResult) sectionResult);
		}
		else
		{
			addNamedResult(name, (SectionRenderable) sectionResult);
		}
	}

	public class CombinedTemplateRenderable extends AbstractCombinedTemplateRenderable
	{
		private String name;

		public CombinedTemplateRenderable(String name)
		{
			this.name = name;
		}

		@Override
		public void realRender(SectionWriter writer) throws IOException
		{
			List<TemplateRenderable> renderers = getRenderers(writer);
			for( TemplateRenderable templateRenderable : renderers )
			{
				if( templateRenderable != null )
				{
					templateRenderable.realRender(writer);
				}
			}
		}

		@Override
		public void preRender(PreRenderContext info)
		{
			List<TemplateRenderable> renderers = getRenderers(info);
			for( TemplateRenderable templateRenderable : renderers )
			{
				if( templateRenderable != null )
				{
					templateRenderable.preRender(info);
				}
			}
		}

		@Override
		protected void setupTemplateRenderables(RenderContext context)
		{
			addTemplateRenderable(getInnerResult(context, name));
			for( TemplateResult templateResult : others )
			{
				TemplateRenderable namedResult = templateResult.getNamedResult(context, name);
				if( namedResult != null )
				{
					addTemplateRenderable(namedResult);
				}
			}
		}
	}
}
